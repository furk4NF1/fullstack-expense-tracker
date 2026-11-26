package com.portfolio.expensetracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "E-posta adresi gerekli.")
        @Email(message = "Geçerli bir e-posta adresi gir.")
        @Size(max = 254, message = "E-posta adresi çok uzun.")
        String email,
        @NotBlank(message = "Şifre gerekli.")
        @Size(min = 10, max = 72, message = "Şifre 10–72 karakter arasında olmalı.")
        String password) {
}
