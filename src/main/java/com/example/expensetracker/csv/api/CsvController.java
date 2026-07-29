package com.example.expensetracker.csv.api;

import com.example.expensetracker.csv.api.dto.CsvImportResult;
import com.example.expensetracker.expense.domain.Expense;
import com.example.expensetracker.expense.repo.ExpenseRepository;
import com.example.expensetracker.expense.repo.ExpenseSpecifications;
import com.example.expensetracker.csv.service.CsvService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/csv")
public class CsvController {

    private final ExpenseRepository expenseRepository;
    private final CsvService csvService;

    public CsvController(ExpenseRepository expenseRepository, CsvService csvService) {
        this.expenseRepository = expenseRepository;
        this.csvService = csvService;
    }

    // US6: export
    @GetMapping(value = "/export", produces = "text/csv")
    public void export(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletResponse response
    ) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=expenses.csv");

        Specification<Expense> spec = Specification.where(ExpenseSpecifications.notDeleted())
                .and(ExpenseSpecifications.dateFrom(from))
                .and(ExpenseSpecifications.dateTo(to));

        List<Expense> expenses = expenseRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "date"));

        try (PrintWriter writer = response.getWriter()) {
            writer.println("amount,date,category,note");
            for (Expense e : expenses) {
                writer.print(escape(e.getAmount()));
                writer.print(',');
                writer.print(escape(e.getDate()));
                writer.print(',');
                writer.print(escape(e.getCategory()));
                writer.print(',');
                writer.println(escape(e.getNote()));
            }
            writer.flush();
        }
    }

    // US6: import
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CsvImportResult importCsv(@RequestPart("file") MultipartFile file) throws IOException {
        return csvService.importCsv(file.getInputStream());
    }

    private static String escape(Object v) {
        if (v == null) return "";
        String s;
        if (v instanceof BigDecimal bd) {
            s = bd.toPlainString();
        } else {
            s = String.valueOf(v);
        }
        boolean needsQuotes = s.contains(",") || s.contains("\n") || s.contains("\"");
        String escaped = s.replace("\"", "\"\"");
        return needsQuotes ? '"' + escaped + '"' : escaped;
    }
}
