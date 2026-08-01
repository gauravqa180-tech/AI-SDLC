package com.example.expensetracker.expense.api;

import com.example.expensetracker.ExpenseTrackerApplication;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(classes = ExpenseTrackerApplication.class)
@AutoConfigureMockMvc
class ExpenseControllerTest {

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

    @Autowired
    MockMvc mvc;

    @Test
    void create_then_update_should_recalculate_monthly_total() throws Exception {
        String createJson = """
                {"amount": 10.50, "date": "2026-01-10", "category": "Food", "note": "Lunch"}
                """;

        String created = mvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn().getResponse().getContentAsString();

        String id = created.replaceAll(".*\"id\":(\\d+).*", "$1");

        mvc.perform(get("/api/expenses/monthly-total").param("month", "2026-01"))
                .andExpect(status().isOk())
                .andExpect(content().string("10.50"));

        String updateJson = """
                {"amount": 20.00, "date": "2026-01-10", "category": "Food", "note": "Lunch updated"}
                """;

        mvc.perform(put("/api/expenses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(20.00)));

        mvc.perform(get("/api/expenses/monthly-total").param("month", "2026-01"))
                .andExpect(status().isOk())
                .andExpect(content().string("20.00"));
    }

    @Test
    void update_invalid_amount_should_return_400_with_field_error() throws Exception {
        String createJson = """
                {"amount": 10.00, "date": "2026-01-10", "category": "Food", "note": "Lunch"}
                """;

        String created = mvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String id = created.replaceAll(".*\"id\":(\\d+).*", "$1");

        String updateJson = """
                {"amount": -1, "date": "2026-01-10", "category": "Food", "note": "bad"}
                """;

        mvc.perform(put("/api/expenses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.fieldViolations", not(empty())))
                .andExpect(jsonPath("$.fieldViolations[*].field", hasItem("amount")));
    }

    @Test
    void create_invalid_date_format_should_return_400() throws Exception {
        String createJson = """
                {"amount": 10.00, "date": "01-10-2026", "category": "Food", "note": "Lunch"}
                """;

        mvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("invalid data format")));
    }
}
