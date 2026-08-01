package com.aisdlc.expensetracker.expense.api;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.aisdlc.expensetracker.expense.repo.ExpenseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ExpenseControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ExpenseRepository repository;

  @Test
  void create_validatesRequiredFields() throws Exception {
    repository.deleteAll();

    mockMvc.perform(post("/api/expenses")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message", is("Validation failed")))
        .andExpect(jsonPath("$.fieldViolations", hasSize(greaterThanOrEqualTo(1))));
  }

  @Test
  void create_rejectsNonPositiveAmount() throws Exception {
    repository.deleteAll();

    String body = """
        {
          \"amount\": 0,
          \"date\": \"2026-01-01\",
          \"category\": \"Food\",
          \"note\": \"x\"
        }
        """;

    mockMvc.perform(post("/api/expenses")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldViolations[*].field", hasItem("amount")));
  }
}
