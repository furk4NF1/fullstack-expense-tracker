package com.portfolio.expensetracker.dto;

import java.math.BigDecimal;

public record BudgetResponse(
        Long categoryId,
        String categoryName,
        String categoryColor,
        String month,
        BigDecimal budgetAmount,
        BigDecimal spentAmount,
        BigDecimal remainingAmount,
        boolean exceeded
) {
}
