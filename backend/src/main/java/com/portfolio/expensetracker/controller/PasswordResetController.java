package com.portfolio.expensetracker.controller;

import com.portfolio.expensetracker.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {
    public record ForgotRequest(@NotBlank @Email @Size(max=254) String email) {}
    public record ResetRequest(@NotBlank @Pattern(regexp="[A-Za-z0-9_-]{43}") String token,
                               @NotBlank @Size(min=10,max=72) String password) {}
    private final PasswordResetService service;
    public PasswordResetController(PasswordResetService service) { this.service=service; }
    @PostMapping("/forgot-password")
    public Map<String,String> forgot(@Valid @RequestBody ForgotRequest body, HttpServletRequest request) {
        service.request(body.email(),request.getRemoteAddr());
        return Map.of("message","Bu e-posta adresiyle etkin bir hesap varsa sıfırlama bağlantısı gönderilecek. Gelen kutunu ve spam klasörünü kontrol et.");
    }
    @PostMapping("/reset-password")
    public Map<String,String> reset(@Valid @RequestBody ResetRequest body, HttpServletRequest request) {
        service.reset(body.token(),body.password(),request.getRemoteAddr());
        return Map.of("message","Şifren yenilendi. Yeni şifrenle giriş yapabilirsin.");
    }
}
