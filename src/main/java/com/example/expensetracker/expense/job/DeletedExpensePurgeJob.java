package com.example.expensetracker.expense.job;

import com.example.expensetracker.expense.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class DeletedExpensePurgeJob {

  private final ExpenseService expenseService;

  /**
   * Purge expired deleted expenses every hour.
   */
  @Scheduled(cron = "0 10 * * * *")
  public void purge() {
    int purged = expenseService.purgeExpiredDeleted();
    if (purged > 0) {
      log.info("Purged {} expired deleted expenses", purged);
    }
  }
}
