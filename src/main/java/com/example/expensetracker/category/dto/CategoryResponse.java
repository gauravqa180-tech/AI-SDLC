package com.example.expensetracker.category.dto;

public record CategoryResponse(
        Long id,
        String name,
        boolean active
) {
}
