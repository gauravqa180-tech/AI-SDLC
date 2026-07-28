package com.ai.sdlc.expensetracker.expense.api;

import com.ai.sdlc.expensetracker.ExpenseTrackerApplication;
import com.ai.sdlc.expensetracker.expense.api.dto.ExpenseCreateRequest;
import com.ai.sdlc.expensetracker.expense.api.dto.ExpenseUpdateRequest;
import com.ai.sdlc.expensetracker.expense.repo.ExpenseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@Testcontainers
@SpringBootTest(classes = ExpenseTrackerApplication.class)
@AutoConfigureMockMvc
class ExpenseControllerTest {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("expense_tracker")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired ExpenseRepository repo;

    @BeforeEach
    void cleanup() {
        repo.deleteAll();
    }

    @Test
    void create_then_update_then_delete() throws Exception {
        var create = new ExpenseCreateRequest(new BigDecimal("12.50"), LocalDate.of(2026, 7, 1), "Food", "Lunch");

        String createdJson = mvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.category", is("Food")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long id = om.readTree(createdJson).get("id").asLong();

        var update = new ExpenseUpdateRequest(new BigDecimal("15.00"), LocalDate.of(2026, 7, 1), "Food", "Lunch+coffee");

        mvc.perform(put("/api/expenses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(15.00)))
                .andExpect(jsonPath("$.note", is("Lunch+coffee")));

        mvc.perform(delete("/api/expenses/{id}", id))
                .andExpect(status().isNoContent());

        mvc.perform(put("/api/expenses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    @Test
    void validation_negative_amount_rejected() throws Exception {
        var create = new ExpenseCreateRequest(new BigDecimal("-1"), LocalDate.of(2026, 7, 1), "Food", "Bad");

        mvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(create)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.fieldErrors", hasSize(greaterThanOrEqualTo(1))));
    }
}
