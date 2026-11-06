package com.portfolio.expensetracker.dto;

import java.math.BigDecimal;
import java.util.List;

public record ExpenseSummaryResponse(
        String month,
        BigDecimal incomeTotal,
        BigDecimal expenseTotal,
        BigDecimal netBalance,
        int transactionCount,
        List<CategorySummaryResponse> categoryTotals
) {
}
