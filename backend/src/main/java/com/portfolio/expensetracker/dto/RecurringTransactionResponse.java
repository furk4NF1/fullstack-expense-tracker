package com.portfolio.expensetracker.dto;

import com.portfolio.expensetracker.entity.TransactionType;
import java.math.BigDecimal;

public record RecurringTransactionResponse(
        Long id, TransactionType type, BigDecimal amount, String description,
        Long categoryId, String categoryName, String categoryColor, int dayOfMonth
) {}
