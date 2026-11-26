package com.portfolio.expensetracker.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public record SavingsGoalRequest(
 @NotBlank @Size(max=120) String name,
 @NotNull @DecimalMin("0.01") @Digits(integer=10, fraction=2) BigDecimal targetAmount,
 @NotNull @DecimalMin("0.00") @Digits(integer=10, fraction=2) BigDecimal savedAmount,
 LocalDate targetDate,
 @PositiveOrZero Long version
) {}
