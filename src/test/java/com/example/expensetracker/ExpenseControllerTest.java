package com.example.expensetracker;

import com.example.expensetracker.domain.ExpenseCategory;
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
@SpringBootTest
@AutoConfigureMockMvc
class ExpenseControllerTest {

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
        r.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @Autowired
    MockMvc mvc;

    @Test
    void create_then_list_with_filter_and_sort() throws Exception {
        String body1 = """
                {"amount": 12.50, "date": "2026-07-01", "category": "%s", "note": "coffee"}
                """.formatted(ExpenseCategory.FOOD.name());
        String body2 = """
                {"amount": 50.00, "date": "2026-07-02", "category": "%s", "note": "gas"}
                """.formatted(ExpenseCategory.TRANSPORT.name());

        mvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()));

        mvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body2))
                .andExpect(status().isOk());

        mvc.perform(get("/api/expenses")
                        .param("category", ExpenseCategory.FOOD.name())
                        .param("sortBy", "amount")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].note", is("coffee")));

        mvc.perform(get("/api/expenses/export")
                        .param("startDate", "2026-07-01")
                        .param("endDate", "2026-07-31"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/csv")))
                .andExpect(content().string(containsString("amount,date,category,note")));
    }

    @Test
    void update_requires_valid_amount() throws Exception {
        String body = """
                {"amount": 10.00, "date": "2026-07-03", "category": "%s", "note": "snack"}
                """.formatted(ExpenseCategory.FOOD.name());

        String resp = mvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String id = resp.replaceAll(".*\"id\":(\d+).*", "$1");

        String badUpdate = """
                {"amount": 0, "date": "2026-07-03", "category": "%s", "note": "snack"}
                """.formatted(ExpenseCategory.FOOD.name());

        mvc.perform(put("/api/expenses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badUpdate))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Validation failed")));
    }
}
