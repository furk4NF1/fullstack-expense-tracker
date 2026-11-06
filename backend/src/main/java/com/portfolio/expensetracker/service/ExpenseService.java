package com.portfolio.expensetracker.service;

import com.portfolio.expensetracker.dto.CategorySummaryResponse;
import com.portfolio.expensetracker.dto.ExpenseRequest;
import com.portfolio.expensetracker.dto.ExpenseResponse;
import com.portfolio.expensetracker.dto.ExpenseSummaryResponse;
import com.portfolio.expensetracker.entity.Category;
import com.portfolio.expensetracker.entity.Expense;
import com.portfolio.expensetracker.entity.TransactionType;
import com.portfolio.expensetracker.exception.ResourceNotFoundException;
import com.portfolio.expensetracker.repository.CategoryRepository;
import com.portfolio.expensetracker.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    public ExpenseService(ExpenseRepository expenseRepository, CategoryRepository categoryRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getAll(YearMonth month) {
        List<Expense> expenses = month == null
                ? expenseRepository.findAllByOrderByExpenseDateDescIdDesc()
                : expenseRepository.findByExpenseDateBetweenOrderByExpenseDateDescIdDesc(
                        month.atDay(1), month.atEndOfMonth());
        return expenses.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ExpenseSummaryResponse getSummary(YearMonth requestedMonth) {
        YearMonth month = requestedMonth == null ? YearMonth.now() : requestedMonth;
        List<Expense> expenses = expenseRepository.findByExpenseDateBetweenOrderByExpenseDateDescIdDesc(
                month.atDay(1), month.atEndOfMonth());

        BigDecimal incomeTotal = BigDecimal.ZERO;
        BigDecimal expenseTotal = BigDecimal.ZERO;
        Map<Long, BigDecimal> totalsByCategory = new TreeMap<>();
        Map<Long, Category> categoriesById = new HashMap<>();

        for (Expense expense : expenses) {
            if (expense.getType() == TransactionType.INCOME) {
                incomeTotal = incomeTotal.add(expense.getAmount());
            } else {
                expenseTotal = expenseTotal.add(expense.getAmount());
                Category category = expense.getCategory();
                totalsByCategory.merge(category.getId(), expense.getAmount(), BigDecimal::add);
                categoriesById.put(category.getId(), category);
            }
        }

        List<CategorySummaryResponse> categoryTotals = new ArrayList<>();
        totalsByCategory.forEach((categoryId, total) -> {
            Category category = categoriesById.get(categoryId);
            categoryTotals.add(new CategorySummaryResponse(
                    categoryId, category.getName(), category.getColor(), total));
        });
        categoryTotals.sort(Comparator.comparing(CategorySummaryResponse::categoryName));

        return new ExpenseSummaryResponse(
                month.toString(), incomeTotal, expenseTotal, incomeTotal.subtract(expenseTotal),
                expenses.size(), List.copyOf(categoryTotals));
    }

    @Transactional
    public ExpenseResponse create(ExpenseRequest request) {
        Category category = findCategory(request.categoryId());
        Expense expense = new Expense(
                request.type(), request.amount(), request.description().trim(), request.expenseDate(), category);
        return toResponse(expenseRepository.save(expense));
    }

    @Transactional
    public ExpenseResponse update(Long id, ExpenseRequest request) {
        Expense expense = findExpense(id);
        Category category = findCategory(request.categoryId());
        expense.update(request.type(), request.amount(), request.description().trim(), request.expenseDate(), category);
        return toResponse(expenseRepository.save(expense));
    }

    @Transactional
    public void delete(Long id) {
        expenseRepository.delete(findExpense(id));
    }

    private Expense findExpense(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bu kayıt bulunamadı."));
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seçilen kategori bulunamadı."));
    }

    private ExpenseResponse toResponse(Expense expense) {
        Category category = expense.getCategory();
        return new ExpenseResponse(
                expense.getId(), expense.getType(), expense.getAmount(), expense.getDescription(),
                expense.getExpenseDate(), category.getId(), category.getName(), category.getColor());
    }
}
