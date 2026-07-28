package com.ai.sdlc.expensetracker.expenses.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expenses", indexes = {
        @Index(name = "idx_expenses_date", columnList = "expense_date"),
        @Index(name = "idx_expenses_category", columnList = "category"),
        @Index(name = "idx_expenses_amount", columnList = "amount")
})
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "expense_date", nullable = false)
    private LocalDate date;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "note", length = 500)
    private String note;

    protected Expense() {
    }

    public Expense(BigDecimal amount, LocalDate date, String category, String note) {
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
