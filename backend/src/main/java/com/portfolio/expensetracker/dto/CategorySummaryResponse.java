package com.portfolio.expensetracker.dto;

import java.math.BigDecimal;

public record CategorySummaryResponse(
        Long categoryId,
        String categoryName,
        String categoryColor,
        BigDecimal totalAmount
) {
}
