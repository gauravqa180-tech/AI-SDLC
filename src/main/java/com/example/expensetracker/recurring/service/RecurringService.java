package com.example.expensetracker.recurring.service;

import com.example.expensetracker.expense.api.dto.ExpenseRequest;
import com.example.expensetracker.expense.api.dto.ExpenseResponse;
import com.example.expensetracker.expense.service.ExpenseService;
import com.example.expensetracker.recurring.api.dto.RecurringTemplateRequest;
import com.example.expensetracker.recurring.domain.RecurringOccurrence;
import com.example.expensetracker.recurring.domain.RecurringTemplate;
import com.example.expensetracker.recurring.repo.RecurringOccurrenceRepository;
import com.example.expensetracker.recurring.repo.RecurringTemplateRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecurringService {

  private final RecurringTemplateRepository templateRepository;
  private final RecurringOccurrenceRepository occurrenceRepository;
  private final ExpenseService expenseService;

  @Transactional
  public RecurringTemplate create(RecurringTemplateRequest req) {
    RecurringTemplate t = new RecurringTemplate();
    apply(t, req);
    return templateRepository.save(t);
  }

  @Transactional
  public RecurringTemplate update(long id, RecurringTemplateRequest req) {
    RecurringTemplate t = templateRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Recurring template not found: " + id));
    apply(t, req);
    return templateRepository.save(t);
  }

  @Transactional
  public void setActive(long id, boolean active) {
    RecurringTemplate t = templateRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Recurring template not found: " + id));
    t.setActive(active);
    templateRepository.save(t);
  }

  @Transactional(readOnly = true)
  public List<RecurringTemplate> list() {
    return templateRepository.findAll();
  }

  /**
   * Generate any due recurring expenses up to 'today'. Idempotent per (template, occurrence_date).
   */
  @Transactional
  public int generateDue(LocalDate today) {
    List<RecurringTemplate> due = templateRepository.findByActiveTrueAndNextRunDateLessThanEqual(today);
    int created = 0;

    for (RecurringTemplate t : due) {
      LocalDate occurrenceDate = t.getNextRunDate();

      boolean already = occurrenceRepository.findByTemplateIdAndOccurrenceDate(t.getId(), occurrenceDate).isPresent();
      if (!already) {
        ExpenseResponse expense = expenseService.create(new ExpenseRequest(
            t.getAmount(),
            occurrenceDate,
            t.getCategory(),
            t.getNote()
        ));

        RecurringOccurrence occ = new RecurringOccurrence();
        occ.setTemplateId(t.getId());
        occ.setOccurrenceDate(occurrenceDate);
        occ.setExpenseId(expense.id());
        occurrenceRepository.save(occ);
        created++;
      }

      // advance nextRunDate for monthly schedule
      t.setNextRunDate(nextMonthlyDate(t.getDayOfMonth(), occurrenceDate));
      templateRepository.save(t);
    }

    return created;
  }

  private LocalDate nextMonthlyDate(int dayOfMonth, LocalDate from) {
    YearMonth next = YearMonth.from(from).plusMonths(1);
    int day = Math.min(Math.max(dayOfMonth, 1), next.lengthOfMonth());
    return next.atDay(day);
  }

  private void apply(RecurringTemplate t, RecurringTemplateRequest req) {
    t.setAmount(req.amount());
    t.setCategory(req.category());
    t.setNote(req.note());
    t.setScheduleType(req.scheduleType());
    t.setDayOfMonth(req.dayOfMonth());
    t.setNextRunDate(req.nextRunDate());
    t.setActive(req.active());
  }
}
