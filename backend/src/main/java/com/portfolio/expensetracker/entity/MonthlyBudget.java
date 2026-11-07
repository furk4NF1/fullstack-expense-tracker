package com.portfolio.expensetracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "monthly_budgets")
public class MonthlyBudget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "budget_month", nullable = false)
    private LocalDate budgetMonth;

    @Column(name = "budget_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    protected MonthlyBudget() {
    }

    public MonthlyBudget(Category category, LocalDate budgetMonth, BigDecimal amount) {
        this.category = category;
        this.budgetMonth = budgetMonth;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public Category getCategory() {
        return category;
    }

    public LocalDate getBudgetMonth() {
        return budgetMonth;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void updateAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
