package com.ai.sdlc.expensetracker.repository;

import com.ai.sdlc.expensetracker.domain.RecurringExpenseRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecurringExpenseRuleRepository extends JpaRepository<RecurringExpenseRule, Long> {
}
