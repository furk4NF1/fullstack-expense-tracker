package com.portfolio.expensetracker.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/** Reject existing sessions when the stored password changed or account was disabled. */
public class PasswordSessionFilter extends OncePerRequestFilter {
    private final JdbcTemplate jdbc;
    public PasswordSessionFilter(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                              FilterChain chain) throws ServletException, IOException {
        var auth=SecurityContextHolder.getContext().getAuthentication();
        if (auth!=null && auth.getPrincipal() instanceof AppUserPrincipal principal) {
            var hashes=jdbc.queryForList("SELECT password_hash FROM users WHERE id=? AND enabled=true",String.class,principal.getId());
            if (hashes.isEmpty() || !hashes.get(0).equals(principal.getPassword())) {
                SecurityContextHolder.clearContext();
                if (request.getSession(false)!=null) request.getSession(false).invalidate();
            }
        }
        chain.doFilter(request,response);
    }
}
