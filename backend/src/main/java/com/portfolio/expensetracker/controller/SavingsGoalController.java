package com.portfolio.expensetracker.controller;
import com.portfolio.expensetracker.dto.*;
import com.portfolio.expensetracker.security.AppUserPrincipal;
import com.portfolio.expensetracker.service.SavingsGoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/savings-goals")
public class SavingsGoalController {
 private final SavingsGoalService service;
 public SavingsGoalController(SavingsGoalService service) { this.service=service; }
 @GetMapping public List<SavingsGoalResponse> list(@AuthenticationPrincipal AppUserPrincipal p) { return service.list(p.getId()); }
 @PostMapping @ResponseStatus(HttpStatus.CREATED)
 public SavingsGoalResponse create(@AuthenticationPrincipal AppUserPrincipal p, @Valid @RequestBody SavingsGoalRequest r) { return service.create(p.getId(),r); }
 @PutMapping("/{id}")
 public SavingsGoalResponse update(@AuthenticationPrincipal AppUserPrincipal p, @PathVariable Long id, @Valid @RequestBody SavingsGoalRequest r) { return service.update(p.getId(),id,r); }
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
 public void delete(@AuthenticationPrincipal AppUserPrincipal p, @PathVariable Long id) { service.delete(p.getId(),id); }
}
