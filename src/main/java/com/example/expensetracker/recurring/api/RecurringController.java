package com.example.expensetracker.recurring.api;

import com.example.expensetracker.recurring.api.dto.RecurringTemplateRequest;
import com.example.expensetracker.recurring.domain.RecurringTemplate;
import com.example.expensetracker.recurring.service.RecurringService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recurring")
@RequiredArgsConstructor
public class RecurringController {

  private final RecurringService recurringService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public RecurringTemplate create(@RequestBody @Valid RecurringTemplateRequest req) {
    return recurringService.create(req);
  }

  @PutMapping("/{id}")
  public RecurringTemplate update(@PathVariable long id, @RequestBody @Valid RecurringTemplateRequest req) {
    return recurringService.update(id, req);
  }

  @GetMapping
  public List<RecurringTemplate> list() {
    return recurringService.list();
  }

  @PostMapping("/{id}/pause")
  @ResponseStatus(HttpStatus.OK)
  public void pause(@PathVariable long id) {
    recurringService.setActive(id, false);
  }

  @PostMapping("/{id}/resume")
  @ResponseStatus(HttpStatus.OK)
  public void resume(@PathVariable long id) {
    recurringService.setActive(id, true);
  }

  @PostMapping("/generate")
  public int generate(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate today) {
    return recurringService.generateDue(today == null ? LocalDate.now() : today);
  }
}
