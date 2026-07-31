package com.kan.expensetracker.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kan.expensetracker.domain.Category;
import com.kan.expensetracker.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ExpenseControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ExpenseRepository expenseRepository;

    @BeforeEach
    void setUp() {
        expenseRepository.deleteAll();
    }

    @Test
    void createUpdateDeleteAndRestore() throws Exception {
        // create
        String createBody = """
                {"amount":10.50,"date":"2026-01-15","category":"%s","note":"coffee"}
                """.formatted(Category.DINING.name());

        String createdJson = mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.amount").value(10.50))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createdJson).get("id").asLong();

        // update
        String updateBody = """
                {"amount":12.00,"date":"2026-01-16","category":"%s","note":"coffee and bagel"}
                """.formatted(Category.DINING.name());

        mockMvc.perform(put("/api/expenses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(12.00))
                .andExpect(jsonPath("$.date").value("2026-01-16"));

        // delete
        mockMvc.perform(delete("/api/expenses/{id}", id))
                .andExpect(status().isNoContent());

        // list should be empty
        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));

        // restore
        mockMvc.perform(post("/api/expenses/{id}/restore", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        // list should contain it again
        String listJson = mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(objectMapper.readTree(listJson).size()).isEqualTo(1);
        assertThat(expenseRepository.findById(id).orElseThrow().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(12.00));
    }
}
