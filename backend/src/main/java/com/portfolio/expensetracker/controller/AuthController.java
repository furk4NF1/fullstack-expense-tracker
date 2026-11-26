package com.portfolio.expensetracker.controller;

import com.portfolio.expensetracker.dto.LoginRequest;
import com.portfolio.expensetracker.dto.RegisterRequest;
import com.portfolio.expensetracker.dto.UserResponse;
import com.portfolio.expensetracker.entity.User;
import com.portfolio.expensetracker.security.AppUserPrincipal;
import com.portfolio.expensetracker.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
    private final CsrfTokenRepository csrfTokenRepository;

    public AuthController(AuthService authService, AuthenticationManager authenticationManager,
                          SecurityContextRepository securityContextRepository,
                          SessionAuthenticationStrategy sessionAuthenticationStrategy,
                          CsrfTokenRepository csrfTokenRepository) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(HttpServletRequest request) {
        org.springframework.security.web.csrf.CsrfToken csrfToken =
                (org.springframework.security.web.csrf.CsrfToken) request.getAttribute(
                        org.springframework.security.web.csrf.CsrfToken.class.getName());
        return Map.of("token", csrfToken.getToken());
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request,
                                      HttpServletRequest servletRequest,
                                      HttpServletResponse servletResponse) {
        User user = authService.register(request.email(), request.password());
        Authentication authentication = authenticate(request.email(), request.password());
        establishSession(authentication, servletRequest, servletResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponse(user.getId(), user.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   HttpServletRequest servletRequest,
                                   HttpServletResponse servletResponse) {
        try {
            Authentication authentication = authenticate(request.email(), request.password());
            establishSession(authentication, servletRequest, servletResponse);
            AppUserPrincipal user = (AppUserPrincipal) authentication.getPrincipal();
            return ResponseEntity.ok(new UserResponse(user.getId(), user.getEmail()));
        } catch (AuthenticationException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "E-posta ya da şifre hatalı."));
        }
    }

    @GetMapping("/me")
    public UserResponse currentUser(Authentication authentication) {
        AppUserPrincipal user = (AppUserPrincipal) authentication.getPrincipal();
        return new UserResponse(user.getId(), user.getEmail());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.clearContext();
        securityContextRepository.saveContext(emptyContext, request, response);
        csrfTokenRepository.saveToken(null, request, response);
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        return ResponseEntity.noContent().build();
    }

    private Authentication authenticate(String email, String password) {
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                email.trim().toLowerCase(Locale.ROOT), password));
    }

    private void establishSession(Authentication authentication, HttpServletRequest request,
                                  HttpServletResponse response) {
        sessionAuthenticationStrategy.onAuthentication(authentication, request, response);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }
}
