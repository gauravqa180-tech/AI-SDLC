package com.ai.sdlc.expensetracker.service;

import org.springframework.data.domain.Sort;

import java.util.*;

public final class SortParser {

    private static final Map<String, String> ALLOWED = Map.of(
            "date", "expenseDate",
            "amount", "amount",
            "category", "category",
            "id", "id"
    );

    private SortParser() {
    }

    /**
     * Accepts formats:
     * - sort=date,desc
     * - sort=amount,asc
     * - sort=date,desc&sort=amount,desc (multiple)
     */
    public static Sort parse(List<String> sortParams, Sort defaultSort) {
        if (sortParams == null || sortParams.isEmpty()) {
            return defaultSort;
        }

        List<Sort.Order> orders = new ArrayList<>();
        for (String raw : sortParams) {
            if (raw == null || raw.isBlank()) continue;
            String[] parts = raw.split(",");
            String key = parts[0].trim();
            String property = ALLOWED.get(key);
            if (property == null) {
                continue; // ignore invalid fields
            }
            Sort.Direction dir = Sort.Direction.DESC;
            if (parts.length > 1) {
                try {
                    dir = Sort.Direction.fromString(parts[1].trim());
                } catch (IllegalArgumentException ignored) {
                }
            }
            orders.add(new Sort.Order(dir, property));
        }
        if (orders.isEmpty()) {
            return defaultSort;
        }
        return Sort.by(orders);
    }
}
