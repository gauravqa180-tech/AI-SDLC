package com.ai.sdlc.expensetracker.api;

import com.ai.sdlc.expensetracker.api.dto.ExpenseCreateRequest;
import com.ai.sdlc.expensetracker.api.dto.ExpenseUpdateRequest;
import com.ai.sdlc.expensetracker.repo.ExpenseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ExpenseControllerIT {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("expense_tracker")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", mysql::getJdbcUrl);
        r.add("spring.datasource.username", mysql::getUsername);
        r.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired ExpenseRepository repo;

    @BeforeEach
    void clean() {
        repo.deleteAll();
    }

    @Test
    void create_then_update_validates_and_persists() throws Exception {
        var create = new ExpenseCreateRequest(new BigDecimal("25.00"), LocalDate.parse("2026-07-01"), "Food", "Lunch");
        String createJson = om.writeValueAsString(create);

        String created = mvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.amount").value(25.00))
                .andReturn().getResponse().getContentAsString();

        long id = om.readTree(created).get("id").asLong();

        var update = new ExpenseUpdateRequest(new BigDecimal("30.00"), LocalDate.parse("2026-07-02"), "Food", "Lunch updated");
        mvc.perform(put("/api/expenses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(30.00))
                .andExpect(jsonPath("$.date").value("2026-07-02"));

        // invalid amount
        var bad = new ExpenseUpdateRequest(new BigDecimal("0"), LocalDate.parse("2026-07-02"), "Food", "");
        mvc.perform(put("/api/expenses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION"));
    }

    @Test
    void list_filter_and_export_csv() throws Exception {
        mvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new ExpenseCreateRequest(new BigDecimal("10.00"), LocalDate.parse("2026-06-01"), "Transport", "Uber"))))
                .andExpect(status().isOk());
        mvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new ExpenseCreateRequest(new BigDecimal("20.00"), LocalDate.parse("2026-06-02"), "Food", "Pizza"))))
                .andExpect(status().isOk());

        mvc.perform(get("/api/expenses")
                        .param("from", "2026-06-01")
                        .param("to", "2026-06-30")
                        .param("category", "Transport")
                        .param("q", "uber"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("Transport"));

        mvc.perform(get("/api/expenses/export")
                        .param("from", "2026-06-01")
                        .param("to", "2026-06-30"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("text/csv")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id,date,amount,category,note")));

        mvc.perform(get("/api/expenses/export")
                        .param("from", "2025-01-01")
                        .param("to", "2025-01-31"))
                .andExpect(status().isNoContent());
    }
}
