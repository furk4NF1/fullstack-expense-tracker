package com.portfolio.expensetracker.repository;

import com.portfolio.expensetracker.entity.RecurringTransaction;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {
    List<RecurringTransaction> findByCategory_Owner_IdOrderByIdAsc(Long ownerId);
    Optional<RecurringTransaction> findByIdAndCategory_Owner_Id(Long id, Long ownerId);
    boolean existsByCategory_IdAndCategory_Owner_Id(Long categoryId, Long ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RecurringTransaction r where r.id = :id and r.category.owner.id = :ownerId")
    Optional<RecurringTransaction> lockByIdAndOwner(@Param("id") Long id, @Param("ownerId") Long ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RecurringTransaction r where r.category.owner.id = :ownerId order by r.id")
    List<RecurringTransaction> lockAllByOwner(@Param("ownerId") Long ownerId);
}
