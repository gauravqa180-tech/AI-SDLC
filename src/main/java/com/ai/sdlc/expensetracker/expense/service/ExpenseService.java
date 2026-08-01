package com.ai.sdlc.expensetracker.expense.service;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ai.sdlc.expensetracker.common.exception.BadRequestException;
import com.ai.sdlc.expensetracker.common.exception.NotFoundException;
import com.ai.sdlc.expensetracker.config.AppProperties;
import com.ai.sdlc.expensetracker.expense.api.dto.DeleteResponse;
import com.ai.sdlc.expensetracker.expense.api.dto.ExpenseCreateRequest;
import com.ai.sdlc.expensetracker.expense.api.dto.ExpenseResponse;
import com.ai.sdlc.expensetracker.expense.api.dto.ExpenseUpdateRequest;
import com.ai.sdlc.expensetracker.expense.api.dto.MonthlySummaryResponse;
import com.ai.sdlc.expensetracker.expense.api.dto.PagedResponse;
import com.ai.sdlc.expensetracker.expense.api.dto.UndoDeleteResponse;
import com.ai.sdlc.expensetracker.expense.model.Expense;
import com.ai.sdlc.expensetracker.expense.repo.ExpenseRepository;
import com.ai.sdlc.expensetracker.expense.repo.ExpenseSpecifications;

@Service
public class ExpenseService {

    private final ExpenseRepository repository;
    private final AppProperties props;
    private final Clock clock;

    public ExpenseService(ExpenseRepository repository, AppProperties props) {
        this.repository = repository;
        this.props = props;
        this.clock = Clock.systemUTC();
    }

    @Transactional
    public ExpenseResponse create(ExpenseCreateRequest req) {
        Instant now = Instant.now(clock);
        Expense e = new Expense(req.amount(), req.date(), req.category().trim(), normalizeNote(req.note()), now);
        Expense saved = repository.save(e);
        return ExpenseMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ExpenseResponse> list(
            Optional<String> q,
            Optional<String> category,
            Optional<LocalDate> startDate,
            Optional<LocalDate> endDate,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(parseDirection(sortDir), mapSort(sortBy)));

        Specification<Expense> spec = Specification.where(ExpenseSpecifications.notDeleted());

        if (q.isPresent() && !q.get().isBlank()) {
            spec = spec.and(ExpenseSpecifications.noteContainsIgnoreCase(q.get().trim()));
        }
        if (category.isPresent() && !category.get().isBlank()) {
            spec = spec.and(ExpenseSpecifications.categoryEquals(category.get().trim()));
        }
        if (startDate.isPresent()) {
            spec = spec.and(ExpenseSpecifications.dateGte(startDate.get()));
        }
        if (endDate.isPresent()) {
            spec = spec.and(ExpenseSpecifications.dateLte(endDate.get()));
        }

        Page<Expense> result = repository.findAll(spec, pageable);
        List<ExpenseResponse> items = result.getContent().stream().map(ExpenseMapper::toResponse).toList();
        return new PagedResponse<>(items, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public ExpenseResponse get(Long id) {
        Expense e = repository.findById(id)
                .filter(x -> x.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));
        return ExpenseMapper.toResponse(e);
    }

    @Transactional
    public ExpenseResponse update(Long id, ExpenseUpdateRequest req) {
        Instant now = Instant.now(clock);
        Expense e = repository.findById(id)
                .filter(x -> x.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));

        e.setAmount(req.amount());
        e.setExpenseDate(req.date());
        e.setCategory(req.category().trim());
        e.setNote(normalizeNote(req.note()));
        e.setUpdatedAt(now);

        return ExpenseMapper.toResponse(repository.save(e));
    }

    @Transactional
    public DeleteResponse delete(Long id) {
        Instant now = Instant.now(clock);
        int updated = repository.softDelete(id, now);
        if (updated == 0) {
            boolean exists = repository.existsById(id);
            if (!exists) throw new NotFoundException("Expense not found: " + id);
            throw new BadRequestException("Expense already deleted: " + id);
        }
        Instant undoUntil = now.plus(props.expenses().undoDeleteWindow());
        return new DeleteResponse(id, true, now, undoUntil);
    }

    @Transactional
    public UndoDeleteResponse undoDelete(Long id) {
        Instant now = Instant.now(clock);
        Expense e = repository.findById(id).orElseThrow(() -> new NotFoundException("Expense not found: " + id));
        if (e.getDeletedAt() == null) {
            return new UndoDeleteResponse(id, true);
        }
        Instant undoUntil = e.getDeletedAt().plus(props.expenses().undoDeleteWindow());
        if (now.isAfter(undoUntil)) {
            throw new BadRequestException("Undo window expired for expense: " + id);
        }
        int updated = repository.undoSoftDelete(id, now);
        return new UndoDeleteResponse(id, updated > 0);
    }

    @Transactional(readOnly = true)
    public MonthlySummaryResponse monthlySummary(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        List<Expense> expenses = repository.findAll(
                Specification.where(ExpenseSpecifications.notDeleted())
                        .and(ExpenseSpecifications.dateGte(start))
                        .and(ExpenseSpecifications.dateLte(end))
        );

        BigDecimal total = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> byCategory = new LinkedHashMap<>();
        for (Expense e : expenses) {
            byCategory.merge(e.getCategory(), e.getAmount(), BigDecimal::add);
        }

        List<MonthlySummaryResponse.CategoryTotal> breakdown = byCategory.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .map(en -> new MonthlySummaryResponse.CategoryTotal(en.getKey(), en.getValue()))
                .toList();

        return new MonthlySummaryResponse(month, total, breakdown);
    }

    @Transactional(readOnly = true)
    public void exportCsv(Optional<LocalDate> startDate, Optional<LocalDate> endDate, PrintWriter writer) {
        Specification<Expense> spec = Specification.where(ExpenseSpecifications.notDeleted());
        if (startDate.isPresent()) spec = spec.and(ExpenseSpecifications.dateGte(startDate.get()));
        if (endDate.isPresent()) spec = spec.and(ExpenseSpecifications.dateLte(endDate.get()));

        List<Expense> expenses = repository.findAll(spec, Sort.by(Sort.Direction.ASC, "expenseDate").and(Sort.by("id")));

        writer.println("id,amount,date,category,note");
        for (Expense e : expenses) {
            writer.print(e.getId());
            writer.print(',');
            writer.print(e.getAmount());
            writer.print(',');
            writer.print(e.getExpenseDate());
            writer.print(',');
            writer.print(csvEscape(e.getCategory()));
            writer.print(',');
            writer.print(csvEscape(e.getNote()));
            writer.println();
        }
        writer.flush();
    }

    private Sort.Direction parseDirection(String dir) {
        return "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
    }

    private String mapSort(String sortBy) {
        if (sortBy == null) return "expenseDate";
        return switch (sortBy) {
            case "date" -> "expenseDate";
            case "amount" -> "amount";
            case "category" -> "category";
            case "createdAt" -> "createdAt";
            default -> "expenseDate";
        };
    }

    private String normalizeNote(String note) {
        if (note == null) return null;
        String trimmed = note.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String csvEscape(String s) {
        if (s == null) return "";
        boolean needsQuoting = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String escaped = s.replace("\"", "\"\"");
        return needsQuoting ? "\"" + escaped + "\"" : escaped;
    }
}
