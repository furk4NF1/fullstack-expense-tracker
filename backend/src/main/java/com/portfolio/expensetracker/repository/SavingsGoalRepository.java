package com.portfolio.expensetracker.repository;
import com.portfolio.expensetracker.entity.SavingsGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {
 List<SavingsGoal> findAllByOwnerIdOrderByIdDesc(Long ownerId);
 Optional<SavingsGoal> findByIdAndOwnerId(Long id, Long ownerId);
}
