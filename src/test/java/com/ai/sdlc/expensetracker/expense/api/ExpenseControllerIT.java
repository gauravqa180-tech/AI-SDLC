package com.ai.sdlc.expensetracker.expense.api;

import com.ai.sdlc.expensetracker.expense.domain.Expense;
import com.ai.sdlc.expensetracker.expense.repo.ExpenseRepository;
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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class ExpenseControllerIT {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4.0")
            .withDatabaseName("expense_tracker")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    ExpenseRepository repo;

    @Test
    void put_updatesExpense() throws Exception {
        Expense saved = repo.save(Expense.builder()
                .amount(new BigDecimal("10.00"))
                .expenseDate(LocalDate.of(2026, 7, 1))
                .category("Food")
                .note("Old")
                .build());

        String body = """
                {
                  \"amount\": 12.50,
                  \"expenseDate\": \"2026-07-02\",
                  \"category\": \"Transport\",
                  \"note\": \"Bus\"
                }
                """;

        mvc.perform(put("/api/v1/expenses/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.amount", is(12.50)))
                .andExpect(jsonPath("$.expenseDate", is("2026-07-02")))
                .andExpect(jsonPath("$.category", is("Transport")))
                .andExpect(jsonPath("$.note", is("Bus")));
    }

    @Test
    void put_returns404_whenNotFound() throws Exception {
        String body = """
                {
                  \"amount\": 12.50,
                  \"expenseDate\": \"2026-07-02\",
                  \"category\": \"Transport\",
                  \"note\": \"Bus\"
                }
                """;

        mvc.perform(put("/api/v1/expenses/{id}", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void put_returns400_whenInvalid() throws Exception {
        Expense saved = repo.save(Expense.builder()
                .amount(new BigDecimal("10.00"))
                .expenseDate(LocalDate.of(2026, 7, 1))
                .category("Food")
                .note("Old")
                .build());

        String body = """
                {
                  \"amount\": 0,
                  \"expenseDate\": null,
                  \"category\": \"\",
                  \"note\": \"x\"
                }
                """;

        mvc.perform(put("/api/v1/expenses/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation failed")));
    }
}
