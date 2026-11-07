package com.portfolio.expensetracker.service;

import com.portfolio.expensetracker.dto.BudgetRequest;
import com.portfolio.expensetracker.dto.BudgetResponse;
import com.portfolio.expensetracker.entity.Category;
import com.portfolio.expensetracker.entity.Expense;
import com.portfolio.expensetracker.entity.MonthlyBudget;
import com.portfolio.expensetracker.entity.TransactionType;
import com.portfolio.expensetracker.exception.ResourceNotFoundException;
import com.portfolio.expensetracker.repository.CategoryRepository;
import com.portfolio.expensetracker.repository.ExpenseRepository;
import com.portfolio.expensetracker.repository.MonthlyBudgetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MonthlyBudgetService {
    private final MonthlyBudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    public MonthlyBudgetService(
            MonthlyBudgetRepository budgetRepository,
            CategoryRepository categoryRepository,
            ExpenseRepository expenseRepository) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgets(YearMonth requestedMonth) {
        YearMonth month = requestedMonth == null ? YearMonth.now() : requestedMonth;
        LocalDate monthStart = month.atDay(1);
        Map<Long, MonthlyBudget> budgetsByCategory = new HashMap<>();
        budgetRepository.findAllByBudgetMonth(monthStart).forEach(budget ->
                budgetsByCategory.put(budget.getCategory().getId(), budget));

        Map<Long, BigDecimal> spendingByCategory = new HashMap<>();
        List<Expense> expenses = expenseRepository.findByExpenseDateBetweenOrderByExpenseDateDescIdDesc(
                monthStart, month.atEndOfMonth());
        for (Expense expense : expenses) {
            if (expense.getType() == TransactionType.EXPENSE) {
                spendingByCategory.merge(
                        expense.getCategory().getId(), expense.getAmount(), BigDecimal::add);
            }
        }

        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(category -> toResponse(
                        category,
                        Optional.ofNullable(budgetsByCategory.get(category.getId())),
                        spendingByCategory.getOrDefault(category.getId(), BigDecimal.ZERO),
                        month))
                .toList();
    }

    @Transactional
    public BudgetResponse saveBudget(Long categoryId, YearMonth requestedMonth, BudgetRequest request) {
        YearMonth month = requestedMonth == null ? YearMonth.now() : requestedMonth;
        Category category = findCategory(categoryId);
        LocalDate monthStart = month.atDay(1);

        MonthlyBudget budget = budgetRepository.findByCategoryAndBudgetMonth(category, monthStart)
                .map(existing -> {
                    existing.updateAmount(request.amount());
                    return existing;
                })
                .orElseGet(() -> new MonthlyBudget(category, monthStart, request.amount()));
        budgetRepository.save(budget);

        BigDecimal spent = expenseRepository.findByExpenseDateBetweenOrderByExpenseDateDescIdDesc(
                        monthStart, month.atEndOfMonth()).stream()
                .filter(expense -> expense.getType() == TransactionType.EXPENSE)
                .filter(expense -> expense.getCategory().getId().equals(categoryId))
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return toResponse(category, Optional.of(budget), spent, month);
    }

    @Transactional
    public void deleteBudget(Long categoryId, YearMonth requestedMonth) {
        YearMonth month = requestedMonth == null ? YearMonth.now() : requestedMonth;
        Category category = findCategory(categoryId);
        budgetRepository.deleteByCategoryAndBudgetMonth(category, month.atDay(1));
    }

    private Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Seçilen kategori bulunamadı."));
    }

    private BudgetResponse toResponse(
            Category category, Optional<MonthlyBudget> budget, BigDecimal spent, YearMonth month) {
        BigDecimal amount = budget.map(MonthlyBudget::getAmount).orElse(null);
        BigDecimal remaining = amount == null ? null : amount.subtract(spent);
        return new BudgetResponse(
                category.getId(), category.getName(), category.getColor(), month.toString(),
                amount, spent, remaining, amount != null && spent.compareTo(amount) > 0);
    }
}
