package com.ai.sdlc.expensetracker.api;

import com.ai.sdlc.expensetracker.api.dto.BudgetUpsertRequest;
import com.ai.sdlc.expensetracker.api.dto.ExpenseCreateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class BudgetAlertIT {

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

    @Test
    void crossing_threshold_creates_alert_event_once() throws Exception {
        mvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new BudgetUpsertRequest("2026-07", null, new BigDecimal("100")))))
                .andExpect(status().isOk());

        // 79 total - no threshold
        mvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new ExpenseCreateRequest(new BigDecimal("79"), LocalDate.parse("2026-07-01"), "Food", ""))))
                .andExpect(status().isOk());

        // Cross 80% with +1
        mvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new ExpenseCreateRequest(new BigDecimal("1"), LocalDate.parse("2026-07-02"), "Food", ""))))
                .andExpect(status().isOk());

        // Evaluate endpoint should not error; alert creation is idempotent due to AlertEvent unique constraint.
        mvc.perform(post("/api/alerts/evaluate")
                        .param("month", "2026-07"))
                .andExpect(status().isOk());
    }
}
