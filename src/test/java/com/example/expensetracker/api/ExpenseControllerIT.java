package com.example.expensetracker.api;

import com.example.expensetracker.api.dto.ExpenseRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ExpenseControllerIT {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @Test
    void shouldRejectInvalidUpdate() throws Exception {
        // create
        ExpenseRequest create = new ExpenseRequest(new BigDecimal("10.00"), LocalDate.now(), "Food", "note", null);
        String createdJson = mvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        long id = om.readTree(createdJson).get("id").asLong();
        long version = om.readTree(createdJson).get("version").asLong();

        // invalid update: amount 0, empty category
        ExpenseRequest update = new ExpenseRequest(new BigDecimal("0.00"), LocalDate.now(), "", "x", version);
        mvc.perform(put("/api/expenses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(update)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldDetectConflictOnVersionMismatch() throws Exception {
        ExpenseRequest create = new ExpenseRequest(new BigDecimal("10.00"), LocalDate.now(), "Food", "note", null);
        String createdJson = mvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = om.readTree(createdJson).get("id").asLong();
        long version = om.readTree(createdJson).get("version").asLong();

        // update once correctly
        ExpenseRequest okUpdate = new ExpenseRequest(new BigDecimal("12.00"), LocalDate.now(), "Food", "note2", version);
        String updatedJson = mvc.perform(put("/api/expenses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(okUpdate)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        long newVersion = om.readTree(updatedJson).get("version").asLong();

        // now attempt update with stale version
        ExpenseRequest staleUpdate = new ExpenseRequest(new BigDecimal("13.00"), LocalDate.now(), "Food", "note3", newVersion - 1);
        mvc.perform(put("/api/expenses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(staleUpdate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }
}
