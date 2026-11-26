package com.portfolio.expensetracker.service;

import com.portfolio.expensetracker.dto.CategorySummaryResponse;
import com.portfolio.expensetracker.dto.AnnualSummaryResponse;
import com.portfolio.expensetracker.dto.ExpenseRequest;
import com.portfolio.expensetracker.dto.ExpenseResponse;
import com.portfolio.expensetracker.dto.ExpenseSummaryResponse;
import com.portfolio.expensetracker.dto.MonthlyTrendResponse;
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
import java.util.Arrays;
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
    public List<ExpenseResponse> getAll(Long ownerId, YearMonth month) {
        List<Expense> expenses = month == null
                ? expenseRepository.findByCategory_Owner_IdOrderByExpenseDateDescIdDesc(ownerId)
                : expenseRepository.findByCategory_Owner_IdAndExpenseDateBetweenOrderByExpenseDateDescIdDesc(
                        ownerId, month.atDay(1), month.atEndOfMonth());
        return expenses.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ExpenseSummaryResponse getSummary(Long ownerId, YearMonth requestedMonth) {
        YearMonth month = requestedMonth == null ? YearMonth.now() : requestedMonth;
        List<Expense> expenses = expenseRepository.findByCategory_Owner_IdAndExpenseDateBetweenOrderByExpenseDateDescIdDesc(
                ownerId, month.atDay(1), month.atEndOfMonth());

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

    @Transactional(readOnly = true)
    public AnnualSummaryResponse getAnnualSummary(Long ownerId, Integer requestedYear) {
        int year = requestedYear == null ? LocalDate.now().getYear() : requestedYear;
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        List<Expense> expenses = expenseRepository.findByCategory_Owner_IdAndExpenseDateBetweenOrderByExpenseDateDescIdDesc(
                ownerId, start, end);

        BigDecimal incomeTotal = BigDecimal.ZERO;
        BigDecimal expenseTotal = BigDecimal.ZERO;
        BigDecimal[] incomeByMonth = new BigDecimal[12];
        BigDecimal[] expensesByMonth = new BigDecimal[12];
        Arrays.fill(incomeByMonth, BigDecimal.ZERO);
        Arrays.fill(expensesByMonth, BigDecimal.ZERO);

        for (Expense expense : expenses) {
            int monthIndex = expense.getExpenseDate().getMonthValue() - 1;
            if (expense.getType() == TransactionType.INCOME) {
                incomeTotal = incomeTotal.add(expense.getAmount());
                incomeByMonth[monthIndex] = incomeByMonth[monthIndex].add(expense.getAmount());
            } else {
                expenseTotal = expenseTotal.add(expense.getAmount());
                expensesByMonth[monthIndex] = expensesByMonth[monthIndex].add(expense.getAmount());
            }
        }

        List<MonthlyTrendResponse> months = new ArrayList<>(12);
        for (int i = 0; i < 12; i++) {
            months.add(new MonthlyTrendResponse(i + 1, incomeByMonth[i], expensesByMonth[i]));
        }

        return new AnnualSummaryResponse(
                year, incomeTotal, expenseTotal, incomeTotal.subtract(expenseTotal), List.copyOf(months));
    }

    @Transactional
    public ExpenseResponse create(Long ownerId, ExpenseRequest request) {
        Category category = findCategory(request.categoryId(), ownerId);
        Expense expense = new Expense(
                request.type(), request.amount(), request.description().trim(), request.expenseDate(), category);
        return toResponse(expenseRepository.save(expense));
    }

    @Transactional
    public ExpenseResponse update(Long ownerId, Long id, ExpenseRequest request) {
        Expense expense = findExpense(id, ownerId);
        Category category = findCategory(request.categoryId(), ownerId);
        expense.update(request.type(), request.amount(), request.description().trim(), request.expenseDate(), category);
        return toResponse(expenseRepository.save(expense));
    }

    @Transactional
    public void delete(Long ownerId, Long id) {
        expenseRepository.delete(findExpense(id, ownerId));
    }

    private Expense findExpense(Long id, Long ownerId) {
        return expenseRepository.findByIdAndCategory_Owner_Id(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Bu kayıt bulunamadı."));
    }

    private Category findCategory(Long id, Long ownerId) {
        return categoryRepository.findByIdAndOwner_Id(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seçilen kategori bulunamadı."));
    }

    private ExpenseResponse toResponse(Expense expense) {
        Category category = expense.getCategory();
        return new ExpenseResponse(
                expense.getId(), expense.getType(), expense.getAmount(), expense.getDescription(),
                expense.getExpenseDate(), category.getId(), category.getName(), category.getColor());
    }
}
