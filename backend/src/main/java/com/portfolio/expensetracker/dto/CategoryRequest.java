package com.portfolio.expensetracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Kategori adı boş olamaz.")
        @Size(max = 80, message = "Kategori adı en fazla 80 karakter olabilir.")
        String name,

        @NotBlank(message = "Kategori rengi seçilmelidir.")
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Renk #RRGGBB biçiminde olmalıdır.")
        String color
) {
}

