package com.ai.sdlc.expensetracker.repository;

import com.ai.sdlc.expensetracker.domain.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {
    Optional<Expense> findByIdAndDeletedAtIsNull(Long id);

    Page<Expense> findAllByDeletedAtIsNull(Pageable pageable);
}
