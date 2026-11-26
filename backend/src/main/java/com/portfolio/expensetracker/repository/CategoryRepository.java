package com.portfolio.expensetracker.repository;

import com.portfolio.expensetracker.entity.Category;
import com.portfolio.expensetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByOwner_IdOrderByNameAsc(Long ownerId);

    boolean existsByOwner_Id(Long ownerId);

    boolean existsByNameIgnoreCaseAndOwner_Id(String name, Long ownerId);

    boolean existsByNameIgnoreCaseAndOwner_IdAndIdNot(String name, Long ownerId, Long id);

    java.util.Optional<Category> findByIdAndOwner_Id(Long id, Long ownerId);

    @Modifying
    @Query("update Category c set c.owner = :newOwner where c.owner.id = :oldOwnerId")
    int transferOwnership(@Param("oldOwnerId") Long oldOwnerId, @Param("newOwner") User newOwner);
}
