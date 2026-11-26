package com.portfolio.expensetracker.service;

import com.portfolio.expensetracker.entity.User;
import com.portfolio.expensetracker.repository.UserRepository;
import com.portfolio.expensetracker.security.AppUserPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AppUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException("Hesap bulunamadı."));
        return new AppUserPrincipal(user.getId(), user.getEmail(), user.getPasswordHash(), user.isEnabled());
    }
}
