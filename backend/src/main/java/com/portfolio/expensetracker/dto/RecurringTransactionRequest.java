package com.portfolio.expensetracker.dto;

import com.portfolio.expensetracker.entity.TransactionType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record RecurringTransactionRequest(
        @NotNull TransactionType type,
        @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal amount,
        @NotBlank @Size(max = 120) String description,
        @NotNull Long categoryId,
        @NotNull @Min(1) @Max(31) Integer dayOfMonth
) {}
