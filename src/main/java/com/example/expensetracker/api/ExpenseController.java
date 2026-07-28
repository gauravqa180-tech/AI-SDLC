package com.example.expensetracker.api;

import com.example.expensetracker.api.dto.*;
import com.example.expensetracker.service.ExpenseService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

  private final ExpenseService expenseService;

  /**
   * User story: search/filter/sort
   * Examples:
   * /api/expenses?start=2026-07-01&end=2026-07-31&category=Food&q=uber&sortBy=date&direction=desc
   */
  @GetMapping
  public List<ExpenseResponse> list(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) BigDecimal minAmount,
      @RequestParam(required = false) BigDecimal maxAmount,
      @RequestParam(required = false, name = "q") String q,
      @RequestParam(required = false, defaultValue = "date") String sortBy,
      @RequestParam(required = false, defaultValue = "desc") String direction
  ) {
    Sort sort = Sort.by(parseDirection(direction), sortProperty(sortBy));
    return expenseService.list(start, end, category, minAmount, maxAmount, q, sort);
  }

  /** User story: add expense */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ExpenseResponse create(@Valid @RequestBody ExpenseCreateRequest req) {
    return expenseService.create(req);
  }

  /** User story: edit expense */
  @PutMapping("/{id}")
  public ExpenseResponse update(@PathVariable long id, @Valid @RequestBody ExpenseUpdateRequest req) {
    return expenseService.update(id, req);
  }

  /** User story: delete + undo (soft delete) */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable long id) {
    expenseService.softDelete(id);
  }

  /** User story: recently deleted */
  @GetMapping("/deleted")
  public List<ExpenseResponse> listDeleted(
      @RequestParam(required = false, defaultValue = "deletedAt") String sortBy,
      @RequestParam(required = false, defaultValue = "desc") String direction
  ) {
    Sort sort = Sort.by(parseDirection(direction), sortProperty(sortBy));
    return expenseService.listDeleted(sort);
  }

  /** User story: restore */
  @PostMapping("/{id}/restore")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void restore(@PathVariable long id) {
    expenseService.restore(id);
  }

  /** User story: monthly total */
  @GetMapping("/monthly-total")
  public MonthlyTotalResponse monthlyTotal(@RequestParam String month) {
    return expenseService.monthlyTotal(month);
  }

  /** User story: monthly report breakdown */
  @GetMapping("/report")
  public ReportResponse report(@RequestParam String month) {
    return expenseService.monthlyReport(month);
  }

  /** User story: export CSV */
  @GetMapping(value = "/export", produces = "text/csv")
  public ResponseEntity<String> exportCsv(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
  ) {
    // Simple CSV string generator (sufficient for v1). For large data, stream output.
    List<ExpenseResponse> expenses = expenseService.list(start, end, null, null, null, null, Sort.by(Sort.Direction.ASC, "date"));

    StringBuilder sb = new StringBuilder();
    sb.append("expenseId,date,amount,category,note\n");
    for (ExpenseResponse e : expenses) {
      sb.append(e.id()).append(',')
          .append(e.date()).append(',')
          .append(e.amount()).append(',')
          .append(escape(e.category())).append(',')
          .append(escape(e.note()))
          .append('\n');
    }

    String filename = "expenses_" + start + "_to_" + end + ".csv";
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .contentType(MediaType.parseMediaType("text/csv"))
        .body(sb.toString());
  }

  private Sort.Direction parseDirection(String direction) {
    return "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
  }

  private String sortProperty(String sortBy) {
    return switch (sortBy == null ? "" : sortBy.toLowerCase()) {
      case "amount" -> "amount";
      case "createdat" -> "createdAt";
      case "updatedat" -> "updatedAt";
      case "deletedat" -> "deletedAt";
      case "date" -> "date";
      default -> "date";
    };
  }

  private String escape(String s) {
    if (s == null) return "";
    String value = s.replace("\"", "\"\"");
    if (value.contains(",") || value.contains("\n") || value.contains("\"") || value.contains("\r")) {
      return "\"" + value + "\"";
    }
    return value;
  }
}
