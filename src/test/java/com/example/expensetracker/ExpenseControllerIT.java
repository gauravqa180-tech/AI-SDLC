package com.example.expensetracker;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ExpenseControllerIT {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper om;

  @Test
  void create_update_delete_restore_flow() throws Exception {
    String created = mvc.perform(post("/api/expenses")
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(Map.of(
                "amount", new BigDecimal("12.50"),
                "date", LocalDate.now().toString(),
                "category", "Food",
                "note", "Lunch"
            ))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andReturn().getResponse().getContentAsString();

    Map<?, ?> createdObj = om.readValue(created, Map.class);
    Integer id = (Integer) createdObj.get("id");
    Integer version = (Integer) createdObj.get("version");

    mvc.perform(put("/api/expenses/" + id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(Map.of(
                "amount", new BigDecimal("15.00"),
                "date", LocalDate.now().toString(),
                "category", "Food",
                "note", "Lunch updated",
                "version", version
            ))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.amount").value(15.00));

    mvc.perform(delete("/api/expenses/" + id))
        .andExpect(status().isNoContent());

    mvc.perform(get("/api/expenses"))
        .andExpect(status().isOk())
        .andExpect(content().string("[]"));

    mvc.perform(get("/api/expenses/deleted"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(id));

    mvc.perform(post("/api/expenses/" + id + "/restore"))
        .andExpect(status().isNoContent());

    mvc.perform(get("/api/expenses"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(id));
  }
}
