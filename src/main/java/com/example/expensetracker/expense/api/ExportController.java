package com.example.expensetracker.expense.api;

import com.example.expensetracker.expense.api.dto.ExpenseResponse;
import com.example.expensetracker.expense.service.ExpenseService;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

  private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_DATE;

  private final ExpenseService expenseService;

  @GetMapping(value = "/expenses.csv", produces = "text/csv")
  public ResponseEntity<byte[]> exportCsv(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
  ) {
    List<ExpenseResponse> rows = expenseService.list(start, end, null, null, "date_desc");
    byte[] csv = toCsv(rows);

    String filename = "expenses_%s_to_%s.csv".formatted(start, end);

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
        .body(csv);
  }

  @GetMapping(value = "/expenses-current-month.csv", produces = "text/csv")
  public ResponseEntity<byte[]> exportCurrentMonth() {
    YearMonth ym = YearMonth.now();
    LocalDate start = ym.atDay(1);
    LocalDate end = ym.atEndOfMonth();
    List<ExpenseResponse> rows = expenseService.list(start, end, null, null, "date_desc");
    byte[] csv = toCsv(rows);

    String filename = "expenses_%s.csv".formatted(ym);

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
        .body(csv);
  }

  private byte[] toCsv(List<ExpenseResponse> rows) {
    StringBuilder sb = new StringBuilder();
    try (PrintWriter pw = new PrintWriter(new java.io.StringWriter())) {
      // no-op: we use StringBuilder directly
    }

    sb.append("id,date,amount,category,note\n");
    for (ExpenseResponse r : rows) {
      sb.append(r.id() == null ? "" : r.id()).append(',');
      sb.append(r.date() == null ? "" : ISO_DATE.format(r.date())).append(',');
      sb.append(r.amount() == null ? "" : r.amount()).append(',');
      sb.append(escape(r.category())).append(',');
      sb.append(escape(r.note())).append('\n');
    }

    return sb.toString().getBytes(StandardCharsets.UTF_8);
  }

  private String escape(String v) {
    if (v == null) return "";
    String s = v;
    boolean needQuotes = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
    if (s.contains("\"")) {
      s = s.replace("\"", "\"\"");
    }
    return needQuotes ? "\"" + s + "\"" : s;
  }
}
