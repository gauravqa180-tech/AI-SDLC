package com.kan.expensetracker.category.service;

public final class CategoryNormalizer {
    private CategoryNormalizer() {}

    public static String normalize(String name) {
        if (name == null) return null;
        return name.trim().toLowerCase();
    }
}
