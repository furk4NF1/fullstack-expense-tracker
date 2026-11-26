package com.portfolio.expensetracker.service;

import com.portfolio.expensetracker.dto.RecurringApplyResponse;
import com.portfolio.expensetracker.dto.RecurringBulkApplyResponse;
import com.portfolio.expensetracker.dto.RecurringTransactionRequest;
import com.portfolio.expensetracker.dto.RecurringTransactionResponse;
import com.portfolio.expensetracker.entity.Category;
import com.portfolio.expensetracker.entity.Expense;
import com.portfolio.expensetracker.entity.RecurringTransaction;
import com.portfolio.expensetracker.entity.RecurringTransactionApplication;
import com.portfolio.expensetracker.exception.ResourceNotFoundException;
import com.portfolio.expensetracker.repository.CategoryRepository;
import com.portfolio.expensetracker.repository.ExpenseRepository;
import com.portfolio.expensetracker.repository.RecurringTransactionApplicationRepository;
import com.portfolio.expensetracker.repository.RecurringTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Service
public class RecurringTransactionService {
    private final RecurringTransactionRepository recurringRepository;
    private final RecurringTransactionApplicationRepository applicationRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    public RecurringTransactionService(RecurringTransactionRepository recurringRepository,
                                       RecurringTransactionApplicationRepository applicationRepository,
                                       CategoryRepository categoryRepository,
                                       ExpenseRepository expenseRepository) {
        this.recurringRepository = recurringRepository;
        this.applicationRepository = applicationRepository;
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
    }

    @Transactional(readOnly = true)
    public List<RecurringTransactionResponse> list(Long ownerId) {
        return recurringRepository.findByCategory_Owner_IdOrderByIdAsc(ownerId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public RecurringTransactionResponse create(Long ownerId, RecurringTransactionRequest request) {
        Category category = findCategory(ownerId, request.categoryId());
        return toResponse(recurringRepository.save(new RecurringTransaction(category, request.type(), request.amount(),
                request.description().trim(), request.dayOfMonth())));
    }

    @Transactional
    public RecurringTransactionResponse update(Long ownerId, Long id, RecurringTransactionRequest request) {
        RecurringTransaction recurring = lockRecurring(ownerId, id);
        Category category = findCategory(ownerId, request.categoryId());
        recurring.update(category, request.type(), request.amount(), request.description().trim(), request.dayOfMonth());
        return toResponse(recurring);
    }

    @Transactional
    public void delete(Long ownerId, Long id) {
        recurringRepository.delete(lockRecurring(ownerId, id));
    }

    @Transactional
    public RecurringApplyResponse apply(Long ownerId, Long id, YearMonth month) {
        return applyLocked(lockRecurring(ownerId, id), month);
    }

    @Transactional
    public RecurringBulkApplyResponse applyAll(Long ownerId, YearMonth month) {
        List<RecurringTransaction> templates = recurringRepository.lockAllByOwner(ownerId);
        int createdCount = 0;
        for (RecurringTransaction template : templates) {
            if (applyLocked(template, month).created()) createdCount++;
        }
        return new RecurringBulkApplyResponse(createdCount, templates.size() - createdCount);
    }

    private RecurringApplyResponse applyLocked(RecurringTransaction recurring, YearMonth month) {
        var existing = applicationRepository.findByRecurring_IdAndApplicationMonth(recurring.getId(), month.atDay(1));
        if (existing.isPresent()) {
            Expense expense = existing.get().getExpense();
            return new RecurringApplyResponse(false, expense == null ? null : expense.getId());
        }
        int day = Math.min(recurring.getDayOfMonth(), month.lengthOfMonth());
        Expense expense = expenseRepository.save(new Expense(recurring.getType(), recurring.getAmount(),
                recurring.getDescription(), month.atDay(day), recurring.getCategory()));
        applicationRepository.save(new RecurringTransactionApplication(recurring, month.atDay(1), expense));
        return new RecurringApplyResponse(true, expense.getId());
    }

    private RecurringTransaction lockRecurring(Long ownerId, Long id) {
        return recurringRepository.lockByIdAndOwner(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Tekrarlayan işlem bulunamadı."));
    }

    private Category findCategory(Long ownerId, Long categoryId) {
        return categoryRepository.findByIdAndOwner_Id(categoryId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seçilen kategori bulunamadı."));
    }

    private RecurringTransactionResponse toResponse(RecurringTransaction recurring) {
        Category category = recurring.getCategory();
        return new RecurringTransactionResponse(recurring.getId(), recurring.getType(), recurring.getAmount(),
                recurring.getDescription(), category.getId(), category.getName(), category.getColor(),
                recurring.getDayOfMonth());
    }
}
