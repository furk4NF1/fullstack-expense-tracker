package com.portfolio.expensetracker.repository;

import com.portfolio.expensetracker.entity.Category;
import com.portfolio.expensetracker.entity.MonthlyBudget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MonthlyBudgetRepository extends JpaRepository<MonthlyBudget, Long> {
    List<MonthlyBudget> findAllByBudgetMonth(LocalDate budgetMonth);

    Optional<MonthlyBudget> findByCategoryAndBudgetMonth(Category category, LocalDate budgetMonth);

    void deleteByCategoryAndBudgetMonth(Category category, LocalDate budgetMonth);
}
