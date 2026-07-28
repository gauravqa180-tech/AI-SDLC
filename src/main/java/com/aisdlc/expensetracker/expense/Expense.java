package com.aisdlc.expensetracker.expense;

import com.aisdlc.expensetracker.category.Category;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expenses", indexes = {
        @Index(name = "idx_expense_date", columnList = "expense_date"),
        @Index(name = "idx_expense_category", columnList = "category_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount", nullable = false, precision = 13, scale = 2)
    private BigDecimal amount;

    @Column(name = "expense_date", nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_expense_category"))
    private Category category;

    @Column(name = "note", length = 255)
    private String note;
}
