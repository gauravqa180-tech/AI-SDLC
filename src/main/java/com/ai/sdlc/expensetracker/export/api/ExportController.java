package com.ai.sdlc.expensetracker.export.api;

import com.ai.sdlc.expensetracker.expense.api.dto.ExpenseResponse;
import com.ai.sdlc.expensetracker.expense.service.ExpenseService;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expenses/export")
@RequiredArgsConstructor
public class ExportController {

  private final ExpenseService expenseService;

  @GetMapping(value = "/csv", produces = "text/csv")
  public org.springframework.http.ResponseEntity<byte[]> exportCsv(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
  ) {
    var expenses = expenseService.list(start, end, null, null, Sort.by(Sort.Direction.ASC, "date"));

    StringBuilder sb = new StringBuilder();
    sb.append("id,date,amount,category,note\n");
    for (ExpenseResponse e : expenses) {
      sb.append(e.id()).append(',')
          .append(e.date()).append(',')
          .append(e.amount()).append(',')
          .append(csv(e.category())).append(',')
          .append(csv(e.note()))
          .append('\n');
    }

    byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
    return org.springframework.http.ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=expenses-" + start + "-to-" + end + ".csv")
        .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
        .body(bytes);
  }

  private static String csv(String v) {
    if (v == null) return "";
    boolean needsQuotes = v.contains(",") || v.contains("\n") || v.contains("\"");
    String escaped = v.replace("\"", "\"\"");
    return needsQuotes ? '"' + escaped + '"' : escaped;
  }
}
