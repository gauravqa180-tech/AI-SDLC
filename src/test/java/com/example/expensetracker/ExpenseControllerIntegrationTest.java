package com.example.expensetracker;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class ExpenseControllerIntegrationTest {

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
    void create_then_update_then_filter_list() throws Exception {
        String createJson = """
                {"amount":12.50,"date":"2026-07-01","category":"Food","note":"lunch"}
                """;

        String updateJson = """
                {"amount":15.00,"date":"2026-07-02","category":"Food","note":"lunch updated"}
                """;

        String created = mvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn().getResponse().getContentAsString();

        String id = created.replaceAll(".*\"id\":(\d+).*", "$1");

        mvc.perform(put("/api/expenses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(15.00))
                .andExpect(jsonPath("$.date").value("2026-07-02"))
                .andExpect(jsonPath("$.note").value("lunch updated"));

        mvc.perform(get("/api/expenses")
                        .param("dateFrom", "2026-07-02")
                        .param("dateTo", "2026-07-02")
                        .param("category", "Food")
                        .param("q", "updated")
                        .param("sortField", "amount")
                        .param("sortOrder", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(Long.parseLong(id)))
                .andExpect(jsonPath("$[0].note").value("lunch updated"));

        mvc.perform(get("/api/expenses/monthly-total")
                        .param("year", "2026")
                        .param("month", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(15.00));
    }

    @Test
    void create_invalid_amount_returns_400() throws Exception {
        String createJson = """
                {"amount":0,"date":"2026-07-01","category":"Food","note":"x"}
                """;

        mvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }
}
