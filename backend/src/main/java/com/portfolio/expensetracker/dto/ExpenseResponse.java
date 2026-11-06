package com.portfolio.expensetracker.dto;

import com.portfolio.expensetracker.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(
        Long id,
        TransactionType type,
        BigDecimal amount,
        String description,
        LocalDate expenseDate,
        Long categoryId,
        String categoryName,
        String categoryColor
) {
}
