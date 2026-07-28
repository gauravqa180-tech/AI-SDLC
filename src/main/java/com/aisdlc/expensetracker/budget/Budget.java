package com.aisdlc.expensetracker.budget;

import com.aisdlc.expensetracker.category.Category;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.YearMonth;

@Entity
@Table(name = "budgets", uniqueConstraints = {
        @UniqueConstraint(name = "uk_budget_month_type_category", columnNames = {"month", "type", "category_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "month", nullable = false, length = 7)
    private String month; // yyyy-MM

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 16)
    private BudgetType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "fk_budget_category"))
    private Category category; // nullable for OVERALL

    @Column(name = "amount", nullable = false, precision = 13, scale = 2)
    private BigDecimal amount;

    public YearMonth monthAsYearMonth() {
        return YearMonth.parse(month);
    }
}
