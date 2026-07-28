package com.example.expensetracker.recurring.job;

import com.example.expensetracker.recurring.service.RecurringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class RecurringGenerationJob {

  private final RecurringService recurringService;

  /**
   * Runs hourly; idempotency is guaranteed by recurring_occurrences unique constraint.
   */
  @Scheduled(cron = "0 0 * * * *")
  public void run() {
    int created = recurringService.generateDue(java.time.LocalDate.now());
    if (created > 0) {
      log.info("Generated {} recurring expenses", created);
    }
  }
}
