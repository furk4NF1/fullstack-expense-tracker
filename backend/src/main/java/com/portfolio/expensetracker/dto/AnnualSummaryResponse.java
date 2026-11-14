package com.portfolio.expensetracker.dto;

import java.math.BigDecimal;
import java.util.List;

public record AnnualSummaryResponse(
        int year,
        BigDecimal incomeTotal,
        BigDecimal expenseTotal,
        BigDecimal netBalance,
        List<MonthlyTrendResponse> months
) {
}

