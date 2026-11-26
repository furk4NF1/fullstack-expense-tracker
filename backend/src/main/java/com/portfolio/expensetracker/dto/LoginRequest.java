package com.portfolio.expensetracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "E-posta adresi gerekli.")
        @Email(message = "Geçerli bir e-posta adresi gir.")
        String email,
        @NotBlank(message = "Şifre gerekli.")
        @Size(max = 72, message = "Şifre çok uzun.")
        String password) {
}
