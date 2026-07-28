package com.ai.sdlc.expensetracker.alert.repo;

import com.ai.sdlc.expensetracker.alert.domain.BudgetAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface BudgetAlertRepository extends JpaRepository<BudgetAlert, Long> {
    boolean existsByMonthAndCategoryAndThreshold(String month, String category, BigDecimal threshold);

    List<BudgetAlert> findAllByMonthOrderByCategoryAscThresholdAsc(String month);
}
