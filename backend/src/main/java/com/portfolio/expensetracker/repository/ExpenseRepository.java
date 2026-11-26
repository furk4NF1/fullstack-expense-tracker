package com.portfolio.expensetracker.repository;

import com.portfolio.expensetracker.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByCategory_Owner_IdOrderByExpenseDateDescIdDesc(Long ownerId);

    List<Expense> findByCategory_Owner_IdAndExpenseDateBetweenOrderByExpenseDateDescIdDesc(
            Long ownerId, LocalDate start, LocalDate end);

    java.util.Optional<Expense> findByIdAndCategory_Owner_Id(Long id, Long ownerId);

    boolean existsByCategory_IdAndCategory_Owner_Id(Long categoryId, Long ownerId);
}
