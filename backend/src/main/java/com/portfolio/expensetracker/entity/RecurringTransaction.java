package com.portfolio.expensetracker.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "recurring_transactions")
public class RecurringTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 10)
    private TransactionType type;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 120)
    private String description;

    @Column(name = "day_of_month", nullable = false)
    private short dayOfMonth;

    protected RecurringTransaction() {}

    public RecurringTransaction(Category category, TransactionType type, BigDecimal amount,
                                String description, int dayOfMonth) {
        update(category, type, amount, description, dayOfMonth);
    }

    public void update(Category category, TransactionType type, BigDecimal amount,
                       String description, int dayOfMonth) {
        this.category = category;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.dayOfMonth = (short) dayOfMonth;
    }

    public Long getId() { return id; }
    public Category getCategory() { return category; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
    public int getDayOfMonth() { return dayOfMonth; }
}
