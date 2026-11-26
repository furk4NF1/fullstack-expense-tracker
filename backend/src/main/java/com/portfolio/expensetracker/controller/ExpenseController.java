package com.portfolio.expensetracker.controller;

import com.portfolio.expensetracker.dto.ExpenseRequest;
import com.portfolio.expensetracker.dto.ExpenseResponse;
import com.portfolio.expensetracker.dto.ExpenseSummaryResponse;
import com.portfolio.expensetracker.dto.AnnualSummaryResponse;
import com.portfolio.expensetracker.service.ExpenseService;
import com.portfolio.expensetracker.security.AppUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public List<ExpenseResponse> getExpenses(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @AuthenticationPrincipal AppUserPrincipal principal) {
        return expenseService.getAll(principal.getId(), month);
    }

    @GetMapping("/summary")
    public ExpenseSummaryResponse getSummary(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @AuthenticationPrincipal AppUserPrincipal principal) {
        return expenseService.getSummary(principal.getId(), month);
    }

    @GetMapping("/annual-summary")
    public AnnualSummaryResponse getAnnualSummary(@RequestParam(required = false) Integer year,
                                                  @AuthenticationPrincipal AppUserPrincipal principal) {
        return expenseService.getAnnualSummary(principal.getId(), year);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse create(@Valid @RequestBody ExpenseRequest request,
                                  @AuthenticationPrincipal AppUserPrincipal principal) {
        return expenseService.create(principal.getId(), request);
    }

    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable Long id, @Valid @RequestBody ExpenseRequest request,
                                  @AuthenticationPrincipal AppUserPrincipal principal) {
        return expenseService.update(principal.getId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal principal) {
        expenseService.delete(principal.getId(), id);
    }
}
