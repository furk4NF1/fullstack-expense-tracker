package com.portfolio.expensetracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "expenses")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 10)
    private TransactionType type;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 120)
    private String description;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected Expense() {
    }

    public Expense(TransactionType type, BigDecimal amount, String description,
                   LocalDate expenseDate, Category category) {
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.expenseDate = expenseDate;
        this.category = category;
    }

    public void update(TransactionType type, BigDecimal amount, String description,
                       LocalDate expenseDate, Category category) {
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.expenseDate = expenseDate;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public Category getCategory() {
        return category;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
