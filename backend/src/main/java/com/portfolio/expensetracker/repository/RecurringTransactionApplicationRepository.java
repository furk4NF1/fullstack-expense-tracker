package com.portfolio.expensetracker.repository;

import com.portfolio.expensetracker.entity.RecurringTransactionApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface RecurringTransactionApplicationRepository extends JpaRepository<RecurringTransactionApplication, Long> {
    Optional<RecurringTransactionApplication> findByRecurring_IdAndApplicationMonth(Long recurringId, LocalDate month);
}
