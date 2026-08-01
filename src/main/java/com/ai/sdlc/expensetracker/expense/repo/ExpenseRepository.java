package com.ai.sdlc.expensetracker.expense.repo;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ai.sdlc.expensetracker.expense.model.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    @Modifying
    @Query("update Expense e set e.deletedAt = :deletedAt, e.updatedAt = :deletedAt where e.id = :id and e.deletedAt is null")
    int softDelete(@Param("id") Long id, @Param("deletedAt") Instant deletedAt);

    @Modifying
    @Query("update Expense e set e.deletedAt = null, e.updatedAt = :updatedAt where e.id = :id and e.deletedAt is not null")
    int undoSoftDelete(@Param("id") Long id, @Param("updatedAt") Instant updatedAt);
}
