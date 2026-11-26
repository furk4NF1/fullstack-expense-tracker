package com.portfolio.expensetracker.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
public record SavingsGoalResponse(Long id, String name, BigDecimal targetAmount,
 BigDecimal savedAmount, LocalDate targetDate, Long version) {}
