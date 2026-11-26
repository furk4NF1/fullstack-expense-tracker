package com.portfolio.expensetracker.controller;

import com.portfolio.expensetracker.dto.RecurringApplyResponse;
import com.portfolio.expensetracker.dto.RecurringBulkApplyResponse;
import com.portfolio.expensetracker.dto.RecurringTransactionRequest;
import com.portfolio.expensetracker.dto.RecurringTransactionResponse;
import com.portfolio.expensetracker.security.AppUserPrincipal;
import com.portfolio.expensetracker.service.RecurringTransactionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/recurring-transactions")
public class RecurringTransactionController {
    private final RecurringTransactionService service;

    public RecurringTransactionController(RecurringTransactionService service) {
        this.service = service;
    }

    @GetMapping
    public List<RecurringTransactionResponse> list(@AuthenticationPrincipal AppUserPrincipal principal) {
        return service.list(principal.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecurringTransactionResponse create(@AuthenticationPrincipal AppUserPrincipal principal,
                                               @Valid @RequestBody RecurringTransactionRequest request) {
        return service.create(principal.getId(), request);
    }

    @PutMapping("/{id}")
    public RecurringTransactionResponse update(@AuthenticationPrincipal AppUserPrincipal principal,
                                               @PathVariable Long id,
                                               @Valid @RequestBody RecurringTransactionRequest request) {
        return service.update(principal.getId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Long id) {
        service.delete(principal.getId(), id);
    }

    @PostMapping("/{id}/apply")
    public RecurringApplyResponse apply(@AuthenticationPrincipal AppUserPrincipal principal,
                                        @PathVariable Long id,
                                        @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return service.apply(principal.getId(), id, month);
    }

    @PostMapping("/apply")
    public RecurringBulkApplyResponse applyAll(@AuthenticationPrincipal AppUserPrincipal principal,
                                               @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return service.applyAll(principal.getId(), month);
    }
}
