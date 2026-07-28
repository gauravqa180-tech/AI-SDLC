package com.example.expensetracker.expense.api;

import com.example.expensetracker.expense.domain.Expense;
import com.example.expensetracker.expense.domain.ExpenseCategory;
import com.example.expensetracker.expense.repo.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ExpenseControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ExpenseRepository expenseRepository;

    @BeforeEach
    void setup() {
        expenseRepository.deleteAll();
    }

    @Test
    void put_updatesExpense_whenVersionMatches() throws Exception {
        Expense saved = expenseRepository.save(Expense.builder()
                .amount(new BigDecimal("10.00"))
                .date(LocalDate.parse("2026-01-10"))
                .category(ExpenseCategory.FOOD)
                .note("lunch")
                .build());

        String body = """
                {
                  \"amount\": 12.50,
                  \"date\": \"2026-01-10\",
                  \"category\": \"FOOD\",
                  \"note\": \"lunch updated\",
                  \"version\": %d
                }
                """.formatted(saved.getVersion());

        mvc.perform(put("/api/v1/expenses/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.amount", is(12.50)))
                .andExpect(jsonPath("$.note", is("lunch updated")))
                .andExpect(jsonPath("$.version", is(notNullValue())));
    }

    @Test
    void put_returns409_whenVersionMismatches() throws Exception {
        Expense saved = expenseRepository.save(Expense.builder()
                .amount(new BigDecimal("10.00"))
                .date(LocalDate.parse("2026-01-10"))
                .category(ExpenseCategory.FOOD)
                .note("lunch")
                .build());

        String body = """
                {
                  \"amount\": 12.50,
                  \"date\": \"2026-01-10\",
                  \"category\": \"FOOD\",
                  \"note\": \"lunch updated\",
                  \"version\": 999
                }
                """;

        mvc.perform(put("/api/v1/expenses/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", containsString("conflict")));
    }

    @Test
    void put_returns400_whenInvalidAmount() throws Exception {
        Expense saved = expenseRepository.save(Expense.builder()
                .amount(new BigDecimal("10.00"))
                .date(LocalDate.parse("2026-01-10"))
                .category(ExpenseCategory.FOOD)
                .note("lunch")
                .build());

        String body = """
                {
                  \"amount\": 0,
                  \"date\": \"2026-01-10\",
                  \"category\": \"FOOD\",
                  \"note\": \"bad\",
                  \"version\": %d
                }
                """.formatted(saved.getVersion());

        mvc.perform(put("/api/v1/expenses/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldViolations", not(empty())))
                .andExpect(jsonPath("$.fieldViolations[*].field", hasItem("amount")));
    }
}
