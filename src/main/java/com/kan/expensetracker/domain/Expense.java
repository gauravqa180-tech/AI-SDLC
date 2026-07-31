package com.kan.expensetracker.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "expenses", indexes = {
        @Index(name = "idx_expenses_date", columnList = "expense_date"),
        @Index(name = "idx_expenses_category", columnList = "category")
})
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "expense_date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 64)
    private Category category;

    @Column(name = "note", length = 1024)
    private String note;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    protected Expense() {
    }

    public Expense(BigDecimal amount, LocalDate date, Category category, String note) {
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public OffsetDateTime getDeletedAt() {
        return deletedAt;
    }

    public void softDelete(OffsetDateTime when) {
        this.deleted = true;
        this.deletedAt = when;
    }

    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
    }
}
