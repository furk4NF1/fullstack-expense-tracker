package com.portfolio.expensetracker.service;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

@Service
public class PasswordResetService {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;
    private final ObjectProvider<JavaMailSender> mail;
    private final TransactionTemplate tx;
    private final boolean enabled;
    private final String baseUrl;
    private final String from;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, Window> limits = new HashMap<>();
    private final ExecutorService worker = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100), r -> {
                Thread thread = new Thread(r, "password-reset-mail");
                thread.setDaemon(true);
                return thread;
            });
    private record Window(long until, int count) {}
    private record Delivery(String email, String token) {}

    public PasswordResetService(JdbcTemplate jdbc, PasswordEncoder encoder,
            ObjectProvider<JavaMailSender> mail, PlatformTransactionManager manager,
            @Value("${app.password-reset.enabled:false}") boolean enabled,
            @Value("${app.password-reset.base-url:http://localhost:4173/}") String baseUrl,
            @Value("${app.password-reset.from:}") String from) {
        this.jdbc=jdbc; this.encoder=encoder; this.mail=mail;
        this.tx=new TransactionTemplate(manager);
        this.enabled=enabled; this.baseUrl=baseUrl; this.from=from;
        if (enabled) {
            URI uri=URI.create(baseUrl);
            boolean local=Set.of("localhost","127.0.0.1").contains(Objects.toString(uri.getHost(),""));
            if ((!uri.getScheme().equals("https") && !(local && uri.getScheme().equals("http")))
                    || uri.getFragment()!=null || uri.getQuery()!=null || uri.getUserInfo()!=null
                    || from.isBlank() || mail.getIfAvailable()==null)
                throw new IllegalStateException("Password reset requires SMTP, a sender and a trusted HTTPS base URL (HTTP only on localhost).");
        }
    }

    // Bounded, per-process limits. The HTTP response never depends on whether the email exists.
    private synchronized boolean allow(String key, int max) {
        long now=System.currentTimeMillis();
        limits.entrySet().removeIf(e -> e.getValue().until() <= now);
        Window current=limits.get(key);
        if (current==null) {
            if (limits.size() >= 10000) return false;
            limits.put(key,new Window(now+900000,1));
            return true;
        }
        if (current.count()>=max) return false;
        limits.put(key,new Window(current.until(),current.count()+1));
        return true;
    }

    public void request(String email, String ip) {
        if (!enabled) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "E-posta ile şifre sıfırlama henüz etkinleştirilmedi.");
        String normalized=email.trim().toLowerCase(Locale.ROOT);
        if (!allow("ip:"+ip,10) || !allow("email:"+hash(normalized),3)) return;
        try { worker.execute(() -> deliver(normalized)); }
        catch (RejectedExecutionException ignored) { log.warn("Password reset queue is full."); }
    }

    private void deliver(String email) {
        Delivery delivery=null;
        try {
            delivery=tx.execute(status -> {
                var users=jdbc.queryForList("SELECT id FROM users WHERE lower(email)=? AND enabled=true AND legacy_data_pending=false FOR UPDATE",email);
                if (users.isEmpty()) return null;
                long id=((Number)users.get(0).get("id")).longValue();
                jdbc.update("DELETE FROM password_reset_tokens WHERE expires_at < CURRENT_TIMESTAMP");
                jdbc.update("DELETE FROM password_reset_tokens WHERE user_id=?",id);
                byte[] bytes=new byte[32]; random.nextBytes(bytes);
                String token=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
                jdbc.update("INSERT INTO password_reset_tokens(token_hash,user_id,expires_at) VALUES (?,?,?)",
                        hash(token),id,java.sql.Timestamp.from(Instant.now().plusSeconds(1800)));
                return new Delivery(email,token);
            });
            if (delivery==null) return;
            SimpleMailMessage message=new SimpleMailMessage();
            message.setFrom(from); message.setTo(delivery.email());
            message.setSubject("Harcama Defterim — Şifreni sıfırla");
            message.setText("Şifreni yenilemek için aşağıdaki bağlantıyı aç. Bağlantı 30 dakika geçerlidir ve yalnızca bir kez kullanılabilir.\n\n"
                    +baseUrl+"#reset-password="+delivery.token()
                    +"\n\nBu isteği sen yapmadıysan e-postayı yok sayabilirsin. Şifren değişmedi.");
            mail.getObject().send(message);
        } catch (Exception failure) {
            if (delivery!=null) {
                try { jdbc.update("DELETE FROM password_reset_tokens WHERE token_hash=?",hash(delivery.token())); }
                catch (Exception ignored) { log.warn("Could not revoke an undelivered reset token."); }
            }
            // Do not log exceptions: SMTP exceptions can contain recipients or message contents.
            log.warn("Password reset delivery failed: {}", deliveryFailureCode(failure));
        }
    }

    static String deliveryFailureCode(Throwable failure) {
        var pending = new ArrayDeque<Throwable>();
        var seen = Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());
        pending.add(failure);
        String code = "MAIL_DELIVERY_FAILED";
        while (!pending.isEmpty() && seen.size() < 32) {
            Throwable current = pending.removeFirst();
            if (!seen.add(current)) continue;
            if (current instanceof org.springframework.mail.MailAuthenticationException
                    || current instanceof jakarta.mail.AuthenticationFailedException)
                return "SMTP_AUTH_REJECTED";
            if (current instanceof javax.net.ssl.SSLException) code = "SMTP_TLS_FAILED";
            else if (current instanceof java.net.UnknownHostException) code = "SMTP_DNS_FAILED";
            else if (current instanceof java.net.SocketTimeoutException) code = "SMTP_TIMEOUT";
            else if (current instanceof java.net.ConnectException) code = "SMTP_CONNECTION_FAILED";
            else if (current instanceof org.springframework.dao.DataAccessException) code = "RESET_DATABASE_FAILED";
            if (current.getCause() != null) pending.add(current.getCause());
            if (current instanceof jakarta.mail.MessagingException message && message.getNextException() != null)
                pending.add(message.getNextException());
            if (current instanceof org.springframework.mail.MailSendException send)
                Collections.addAll(pending, send.getMessageExceptions());
        }
        return code;
    }

    public void reset(String token, String password, String ip) {
        if (!allow("reset:"+ip,30))
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Çok fazla deneme. 15 dakika sonra tekrar dene.");
        if (password.getBytes(StandardCharsets.UTF_8).length>72)
            throw new IllegalArgumentException("Şifre en fazla 72 bayt olabilir.");
        String digest=hash(token);
        String encoded=encoder.encode(password);
        tx.executeWithoutResult(status -> {
            var ids=jdbc.queryForList("SELECT user_id FROM password_reset_tokens WHERE token_hash=?",Long.class,digest);
            if (ids.isEmpty()) throw invalid();
            long id=ids.get(0);
            // Same lock order as issuance; serialize resets and replacement links for this account.
            var users=jdbc.queryForList("SELECT id FROM users WHERE id=? AND enabled=true FOR UPDATE",id);
            if (users.isEmpty()) throw invalid();
            int removed=jdbc.update("DELETE FROM password_reset_tokens WHERE token_hash=? AND expires_at>CURRENT_TIMESTAMP",digest);
            if (removed!=1) throw invalid();
            jdbc.update("UPDATE users SET password_hash=? WHERE id=?",encoded,id);
            jdbc.update("DELETE FROM password_reset_tokens WHERE user_id=?",id);
        });
    }
    private IllegalArgumentException invalid() {
        return new IllegalArgumentException("Bağlantı geçersiz veya süresi dolmuş. Yeni bağlantı iste.");
    }
    private static String hash(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    @PreDestroy public void stop() { worker.shutdownNow(); }
}
