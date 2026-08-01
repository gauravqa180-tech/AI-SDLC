package com.aisdlc.expensetracker.expense.api;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ExpenseControllerIT {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("expense_tracker")
            .withUsername("expense")
            .withPassword("expense");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    MockMvc mockMvc;

    @Test
    void create_then_update_and_monthly_total_reflects_change() throws Exception {
        // create
        String createJson = """
                {"amount":25.00,"expenseDate":"2026-08-01","category":"Food","note":"Lunch"}
                """;

        String response = mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount", is(25.00)))
                .andExpect(jsonPath("$.category", is("Food")))
                .andReturn().getResponse().getContentAsString();

        // naive extract id
        long id = Long.parseLong(response.replaceAll(".*\"id\":(\\d+).*", "$1"));

        // update
        String updateJson = """
                {"amount":30.00,"expenseDate":"2026-08-01","category":"Food","note":"Lunch"}
                """;

        mockMvc.perform(put("/api/expenses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(30.00)));

        // monthly total
        mockMvc.perform(get("/api/expenses/monthly-total")
                        .param("year", "2026")
                        .param("month", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(30.00)));
    }

    @Test
    void update_invalid_amount_returns_400() throws Exception {
        String createJson = """
                {"amount":25.00,"expenseDate":"2026-08-01","category":"Food","note":"Lunch"}
                """;
        String response = mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = Long.parseLong(response.replaceAll(".*\"id\":(\\d+).*", "$1"));

        String updateJson = """
                {"amount":null,"expenseDate":"2026-08-01","category":"Food","note":"Lunch"}
                """;

        mockMvc.perform(put("/api/expenses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation failed")));
    }
}
