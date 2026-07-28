package com.ai.sdlc.expensetracker.api;

import org.springframework.data.domain.Sort;

import java.util.Set;

public final class SortParser {

    private static final Set<String> ALLOWED_FIELDS = Set.of("date", "amount", "category", "id");

    private SortParser() {
    }

    /**
     * Parse sort param in the form: field,(asc|desc)
     * Examples:
     * - date,desc
     * - amount,asc
     */
    public static Sort parse(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "date").and(Sort.by(Sort.Direction.DESC, "id"));
        }

        String[] parts = sort.split(",");
        String field = parts[0].trim();
        String direction = parts.length > 1 ? parts[1].trim() : "asc";

        if (!ALLOWED_FIELDS.contains(field)) {
            throw new IllegalArgumentException("Unsupported sort field: " + field);
        }

        Sort.Direction dir = Sort.Direction.fromOptionalString(direction).orElseThrow(
                () -> new IllegalArgumentException("Unsupported sort direction: " + direction)
        );

        return Sort.by(dir, field).and(Sort.by(Sort.Direction.DESC, "id"));
    }
}
