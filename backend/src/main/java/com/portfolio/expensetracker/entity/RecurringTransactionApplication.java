package com.portfolio.expensetracker.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "recurring_transaction_applications")
public class RecurringTransactionApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recurring_id", nullable = false)
    private RecurringTransaction recurring;

    @Column(name = "application_month", nullable = false)
    private LocalDate applicationMonth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id")
    private Expense expense;

    protected RecurringTransactionApplication() {}

    public RecurringTransactionApplication(RecurringTransaction recurring, LocalDate applicationMonth, Expense expense) {
        this.recurring = recurring;
        this.applicationMonth = applicationMonth;
        this.expense = expense;
    }

    public Expense getExpense() { return expense; }
}
