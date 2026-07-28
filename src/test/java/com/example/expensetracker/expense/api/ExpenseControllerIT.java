package com.example.expensetracker.expense.api;

import com.example.expensetracker.expense.api.dto.ExpenseCreateRequest;
import com.example.expensetracker.expense.api.dto.ExpenseUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ExpenseControllerIT {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
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

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @Test
    void create_and_update_with_optimistic_lock_version() throws Exception {
        var create = new ExpenseCreateRequest(new BigDecimal("25.00"), LocalDate.parse("2026-07-01"), "Food", "Lunch");
        String createdJson = mvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(create)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.version").exists())
                .andReturn().getResponse().getContentAsString();

        long id = om.readTree(createdJson).get("id").asLong();
        long version = om.readTree(createdJson).get("version").asLong();

        var update = new ExpenseUpdateRequest(new BigDecimal("30.00"), LocalDate.parse("2026-07-01"), "Food", "Lunch updated", version);

        mvc.perform(put("/api/expenses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(30.00)))
                .andExpect(jsonPath("$.note", is("Lunch updated")));

        // stale version -> conflict
        var stale = new ExpenseUpdateRequest(new BigDecimal("31.00"), LocalDate.parse("2026-07-01"), "Food", "stale", version);
        mvc.perform(put("/api/expenses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(stale)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("modified")));
    }

    @Test
    void list_with_filters_and_sorting() throws Exception {
        mvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new ExpenseCreateRequest(new BigDecimal("10.00"), LocalDate.parse("2026-07-01"), "Food", null))));
        mvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new ExpenseCreateRequest(new BigDecimal("50.00"), LocalDate.parse("2026-07-02"), "Transport", null))));

        mvc.perform(get("/api/expenses")
                        .param("startDate", "2026-07-01")
                        .param("endDate", "2026-07-31")
                        .param("sortBy", "amount")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].amount", is(50.00)))
                .andExpect(jsonPath("$[1].amount", is(10.00)));

        mvc.perform(get("/api/expenses")
                        .param("category", "Food"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category", is("Food")));
    }

    @Test
    void validation_error_on_negative_amount() throws Exception {
        var bad = new ExpenseCreateRequest(new BigDecimal("-1.00"), LocalDate.parse("2026-07-01"), "Food", null);
        mvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation failed")));
    }
}
