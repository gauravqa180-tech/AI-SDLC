package com.aisdlc.expensetracker.budget;

import com.aisdlc.expensetracker.category.Category;
import com.aisdlc.expensetracker.category.CategoryService;
import com.aisdlc.expensetracker.budget.dto.BudgetProgressResponse;
import com.aisdlc.expensetracker.budget.dto.BudgetResponse;
import com.aisdlc.expensetracker.budget.dto.BudgetUpsertRequest;
import com.aisdlc.expensetracker.expense.Expense;
import com.aisdlc.expensetracker.expense.ExpenseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private static final int THRESHOLD_WARN = 80;
    private static final int THRESHOLD_EXCEEDED = 100;

    private final BudgetRepository budgetRepository;
    private final BudgetAlertRepository budgetAlertRepository;
    private final ExpenseRepository expenseRepository;
    private final CategoryService categoryService;

    @Transactional
    public BudgetResponse upsertOverall(YearMonth month, BudgetUpsertRequest req) {
        String m = month.toString();
        Budget budget = budgetRepository.findByMonthAndTypeAndCategoryIsNull(m, BudgetType.OVERALL)
                .orElseGet(() -> Budget.builder().month(m).type(BudgetType.OVERALL).build());
        budget.setAmount(req.amount());
        Budget saved = budgetRepository.save(budget);
        return toResponse(saved);
    }

    @Transactional
    public BudgetResponse upsertCategory(YearMonth month, Long categoryId, BudgetUpsertRequest req) {
        String m = month.toString();
        Category category = categoryService.getEntity(categoryId);

        Budget budget = budgetRepository.findByMonthAndTypeAndCategory_Id(m, BudgetType.CATEGORY, categoryId)
                .orElseGet(() -> Budget.builder().month(m).type(BudgetType.CATEGORY).category(category).build());

        budget.setCategory(category);
        budget.setAmount(req.amount());
        Budget saved = budgetRepository.save(budget);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public BudgetProgressResponse progress(YearMonth month) {
        String m = month.toString();
        DateRange range = DateRange.forMonth(month);

        List<Expense> expenses = expenseRepository.findAll((root, query, cb) -> cb.between(root.get("date"), range.start(), range.end()));

        BigDecimal totalSpent = expenses.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Long, BigDecimal> spentByCategory = new HashMap<>();
        Map<Long, String> categoryNames = new HashMap<>();
        for (Expense e : expenses) {
            Long cid = e.getCategory().getId();
            spentByCategory.merge(cid, e.getAmount(), BigDecimal::add);
            categoryNames.putIfAbsent(cid, e.getCategory().getName());
        }

        Optional<Budget> overallBudget = budgetRepository.findByMonthAndTypeAndCategoryIsNull(m, BudgetType.OVERALL);
        BudgetProgressResponse.Progress overallProgress = overallBudget
                .map(b -> computeProgress("OVERALL", null, null, b.getAmount(), totalSpent))
                .orElse(null);

        List<Budget> categoryBudgets = budgetRepository.findByMonth(m).stream()
                .filter(b -> b.getType() == BudgetType.CATEGORY)
                .toList();

        List<BudgetProgressResponse.Progress> byCategory = categoryBudgets.stream()
                .map(b -> {
                    Long cid = b.getCategory().getId();
                    BigDecimal spent = spentByCategory.getOrDefault(cid, BigDecimal.ZERO);
                    return computeProgress("CATEGORY", cid, b.getCategory().getName(), b.getAmount(), spent);
                })
                .sorted(Comparator.comparing(BudgetProgressResponse.Progress::percent).reversed())
                .toList();

        List<String> alerts = new ArrayList<>();
        overallBudget.ifPresent(b -> alerts.addAll(checkAndEmitAlerts(m, BudgetType.OVERALL, null, totalSpent, b.getAmount())));
        for (Budget b : categoryBudgets) {
            Long cid = b.getCategory().getId();
            BigDecimal spent = spentByCategory.getOrDefault(cid, BigDecimal.ZERO);
            alerts.addAll(checkAndEmitAlerts(m, BudgetType.CATEGORY, cid, spent, b.getAmount()));
        }

        return new BudgetProgressResponse(month, overallProgress, byCategory, alerts);
    }

    @Transactional
    public void acknowledgeAlert(Long alertId) {
        BudgetAlert alert = budgetAlertRepository.findById(alertId)
                .orElseThrow(() -> new EntityNotFoundException("Alert not found: " + alertId));
        alert.setAcknowledged(true);
        budgetAlertRepository.save(alert);
    }

    private List<String> checkAndEmitAlerts(String month, BudgetType type, Long categoryId, BigDecimal spent, BigDecimal budget) {
        if (budget == null || budget.compareTo(BigDecimal.ZERO) <= 0) return List.of();

        int percent = spent.multiply(BigDecimal.valueOf(100))
                .divide(budget, 0, RoundingMode.DOWN)
                .intValue();

        List<String> messages = new ArrayList<>();
        if (percent >= THRESHOLD_WARN) {
            maybeCreateAlert(month, type, categoryId, THRESHOLD_WARN)
                    .ifPresent(a -> messages.add(messageFor(type, categoryId, THRESHOLD_WARN)));
        }
        if (percent >= THRESHOLD_EXCEEDED) {
            maybeCreateAlert(month, type, categoryId, THRESHOLD_EXCEEDED)
                    .ifPresent(a -> messages.add(messageFor(type, categoryId, THRESHOLD_EXCEEDED)));
        }
        return messages;
    }

    private Optional<BudgetAlert> maybeCreateAlert(String month, BudgetType type, Long categoryId, int threshold) {
        Optional<BudgetAlert> existing = budgetAlertRepository.findByMonthAndTypeAndCategoryIdAndThreshold(month, type, categoryId, threshold);
        if (existing.isPresent()) {
            return Optional.empty();
        }
        BudgetAlert created = budgetAlertRepository.save(BudgetAlert.builder()
                .month(month)
                .type(type)
                .categoryId(categoryId)
                .threshold(threshold)
                .acknowledged(false)
                .build());
        return Optional.of(created);
    }

    private String messageFor(BudgetType type, Long categoryId, int threshold) {
        String scope = (type == BudgetType.OVERALL) ? "Overall" : ("Category " + categoryId);
        return switch (threshold) {
            case THRESHOLD_WARN -> scope + " budget reached 80%";
            case THRESHOLD_EXCEEDED -> scope + " budget exceeded";
            default -> scope + " budget alert";
        };
    }

    private BudgetProgressResponse.Progress computeProgress(String type, Long categoryId, String categoryName, BigDecimal budget, BigDecimal spent) {
        BigDecimal remaining = budget.subtract(spent);
        int percent = spent.multiply(BigDecimal.valueOf(100))
                .divide(budget, 0, RoundingMode.DOWN)
                .intValue();
        return new BudgetProgressResponse.Progress(
                type,
                categoryId,
                categoryName,
                budget,
                spent,
                remaining,
                percent
        );
    }

    private BudgetResponse toResponse(Budget b) {
        return new BudgetResponse(
                b.getId(),
                b.monthAsYearMonth(),
                b.getType().name(),
                b.getCategory() == null ? null : b.getCategory().getId(),
                b.getCategory() == null ? null : b.getCategory().getName(),
                b.getAmount()
        );
    }

    private record DateRange(LocalDate start, LocalDate end) {
        static DateRange forMonth(YearMonth month) {
            return new DateRange(month.atDay(1), month.atEndOfMonth());
        }
    }
}
