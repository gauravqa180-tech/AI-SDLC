package com.ai.sdlc.expensetracker.repo;

import com.ai.sdlc.expensetracker.domain.AlertEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlertEventRepository extends JpaRepository<AlertEvent, Long> {

    Optional<AlertEvent> findByMonthAndScopeAndCategoryAndThreshold(String month, String scope, String category, Double threshold);
}
