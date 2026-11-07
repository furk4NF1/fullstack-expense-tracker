package com.portfolio.expensetracker.controller;

import com.portfolio.expensetracker.dto.BudgetRequest;
import com.portfolio.expensetracker.dto.BudgetResponse;
import com.portfolio.expensetracker.service.MonthlyBudgetService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@CrossOrigin(origins = "http://localhost:5173")
public class MonthlyBudgetController {
    private final MonthlyBudgetService budgetService;

    public MonthlyBudgetController(MonthlyBudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public List<BudgetResponse> getBudgets(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return budgetService.getBudgets(month);
    }

    @PutMapping("/{categoryId}")
    public BudgetResponse saveBudget(
            @PathVariable Long categoryId,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @Valid @RequestBody BudgetRequest request) {
        return budgetService.saveBudget(categoryId, month, request);
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBudget(
            @PathVariable Long categoryId,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        budgetService.deleteBudget(categoryId, month);
    }
}
