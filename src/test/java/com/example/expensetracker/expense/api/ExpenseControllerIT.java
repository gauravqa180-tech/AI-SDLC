package com.example.expensetracker.expense.api;

import com.example.expensetracker.expense.api.dto.ExpenseRequest;
import com.example.expensetracker.expense.domain.Expense;
import com.example.expensetracker.expense.repo.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ExpenseControllerIT {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("expense_tracker")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired MockMvc mvc;
    @Autowired ExpenseRepository repo;

    @BeforeEach
    void setup() {
        repo.deleteAll();
    }

    @Test
    void updateExpense_put_updatesFields() throws Exception {
        Expense saved = repo.save(Expense.builder()
                .amount(new BigDecimal("10.00"))
                .date(LocalDate.of(2026, 1, 10))
                .category("Food")
                .note("old")
                .build());

        String body = """
                {
                  "amount": 12.50,
                  "date": "2026-01-11",
                  "category": "  Groceries  ",
                  "note": "new note"
                }
                """;

        mvc.perform(put("/api/expenses/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.amount", is(12.50)))
                .andExpect(jsonPath("$.date", is("2026-01-11")))
                .andExpect(jsonPath("$.category", is("Groceries")))
                .andExpect(jsonPath("$.note", is("new note")));
    }

    @Test
    void updateExpense_put_nonExisting_returns404() throws Exception {
        String body = """
                {
                  "amount": 12.50,
                  "date": "2026-01-11",
                  "category": "Groceries",
                  "note": "new note"
                }
                """;

        mvc.perform(put("/api/expenses/{id}", 9999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Expense not found")));
    }

    @Test
    void updateExpense_put_invalid_returns400() throws Exception {
        Expense saved = repo.save(Expense.builder()
                .amount(new BigDecimal("10.00"))
                .date(LocalDate.of(2026, 1, 10))
                .category("Food")
                .note("old")
                .build());

        String body = """
                {
                  "amount": 0,
                  "date": null,
                  "category": "",
                  "note": "x"
                }
                """;

        mvc.perform(put("/api/expenses/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldViolations", not(empty())));
    }
}
