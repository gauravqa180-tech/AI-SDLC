package com.ai.sdlc.expensetracker.api;

import com.ai.sdlc.expensetracker.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ExpenseControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ExpenseRepository expenseRepository;

    @BeforeEach
    void setup() {
        expenseRepository.deleteAll();
    }

    @Test
    void shouldRejectInvalidCreate() throws Exception {
        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"amount\":0," +
                                "\"date\":null," +
                                "\"category\":\"\"" +
                                "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.violations.length()").value(3));
    }

    @Test
    void createUpdateListAndExportCsvHappyPath() throws Exception {
        // create
        String createJson = "{" +
                "\"amount\":12.50," +
                "\"date\":\"2026-01-10\"," +
                "\"category\":\"Food\"," +
                "\"note\":\"Lunch\"" +
                "}";

        String response = mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        String id = response.replaceAll(".*\"id\":(\\d+).*", "$1");

        // update
        String updateJson = "{" +
                "\"amount\":15.00," +
                "\"date\":\"2026-01-10\"," +
                "\"category\":\"Food\"," +
                "\"note\":\"Lunch (updated)\"" +
                "}";

        mockMvc.perform(put("/api/expenses/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(15.00));

        // list with search
        mockMvc.perform(get("/api/expenses")
                        .param("q", "updated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(Long.parseLong(id)));

        // monthly breakdown
        mockMvc.perform(get("/api/insights/monthly/category-breakdown")
                        .param("month", "2026-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyTotal").value(15.00))
                .andExpect(jsonPath("$.byCategory[0].category").value("Food"));

        // csv
        mockMvc.perform(get("/api/expenses/export.csv")
                        .param("from", "2026-01-01")
                        .param("to", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id,date,category,amount,note")));
    }
}
