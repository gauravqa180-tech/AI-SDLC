package com.kan.expensetracker.category.api.dto;

public record CategoryResponse(
        Long id,
        String name,
        boolean active
) {
}
