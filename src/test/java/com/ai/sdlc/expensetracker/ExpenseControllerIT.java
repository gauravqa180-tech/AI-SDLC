package com.ai.sdlc.expensetracker;

import com.ai.sdlc.expensetracker.domain.ExpenseCategory;
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
class ExpenseControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    void create_then_update_validates_and_updates() throws Exception {
        String createJson = """
                {
                  \"amount\": 12.50,
                  \"date\": \"2026-01-10\",
                  \"category\": \"GROCERIES\",
                  \"note\": \"milk\"
                }
                """;

        String id = mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll(".*\\\"id\\\"\\s*:\\s*(\\d+).*", "$1");

        String updateJson = """
                {
                  \"amount\": 20.00,
                  \"date\": \"2026-01-11\",
                  \"category\": \"%s\",
                  \"note\": \"updated\"
                }
                """.formatted(ExpenseCategory.DINING.name());

        mockMvc.perform(put("/api/expenses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(20.00))
                .andExpect(jsonPath("$.date").value("2026-01-11"))
                .andExpect(jsonPath("$.category").value("DINING"))
                .andExpect(jsonPath("$.note").value("updated"));

        String invalidUpdate = """
                {
                  \"amount\": 0,
                  \"date\": \"2026-01-11\",
                  \"category\": \"DINING\",
                  \"note\": \"bad\"
                }
                """;

        mockMvc.perform(put("/api/expenses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidUpdate))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.amount").exists());
    }
}
