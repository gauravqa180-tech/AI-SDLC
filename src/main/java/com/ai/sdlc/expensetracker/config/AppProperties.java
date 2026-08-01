package com.ai.sdlc.expensetracker.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Expenses expenses) {

    public record Expenses(Duration undoDeleteWindow) {
    }
}
