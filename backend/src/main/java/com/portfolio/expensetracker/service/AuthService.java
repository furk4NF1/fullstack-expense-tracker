package com.portfolio.expensetracker.service;

import com.portfolio.expensetracker.entity.Category;
import com.portfolio.expensetracker.entity.User;
import com.portfolio.expensetracker.exception.DuplicateResourceException;
import com.portfolio.expensetracker.repository.CategoryRepository;
import com.portfolio.expensetracker.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Service
public class AuthService {
    private static final String RESERVED_LEGACY_EMAIL = "legacy-data@internal.invalid";
    private static final List<String[]> DEFAULT_CATEGORIES = List.of(
            new String[]{"Gıda", "#2b7a66"},
            new String[]{"Ulaşım", "#5273b8"},
            new String[]{"Faturalar", "#d48b3d"},
            new String[]{"Sağlık", "#c26068"},
            new String[]{"Eğlence", "#8a6bb4"},
            new String[]{"Diğer", "#748078"});

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, CategoryRepository categoryRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (normalizedEmail.equals(RESERVED_LEGACY_EMAIL)) {
            throw new DuplicateResourceException("Bu e-posta adresi kullanılamıyor.");
        }
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateResourceException("Bu e-posta adresiyle bir hesap zaten var.");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Şifre 72 bayttan kısa olmalı; daha uzun bir şifre seç.");
        }

        User user = userRepository.save(new User(normalizedEmail, passwordEncoder.encode(password)));
        userRepository.lockPendingLegacyAccount().ifPresent(legacyUser -> {
            categoryRepository.transferOwnership(legacyUser.getId(), user);
            userRepository.delete(legacyUser);
        });

        if (!categoryRepository.existsByOwner_Id(user.getId())) {
            List<Category> defaults = DEFAULT_CATEGORIES.stream()
                    .map(item -> new Category(item[0], item[1], user))
                    .toList();
            categoryRepository.saveAll(defaults);
        }
        return user;
    }
}
