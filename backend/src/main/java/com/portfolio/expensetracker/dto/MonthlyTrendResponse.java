package com.portfolio.expensetracker.dto;

import java.math.BigDecimal;

public record MonthlyTrendResponse(
        int month,
        BigDecimal incomeTotal,
        BigDecimal expenseTotal
) {
}

