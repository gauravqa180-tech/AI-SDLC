package com.ai.sdlc.expensetracker.expense;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.ai.sdlc.expensetracker.expense.api.dto.DeleteResponse;
import com.ai.sdlc.expensetracker.expense.api.dto.ExpenseCreateRequest;
import com.ai.sdlc.expensetracker.expense.api.dto.ExpenseResponse;
import com.ai.sdlc.expensetracker.expense.api.dto.ExpenseUpdateRequest;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExpenseFlowIT {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("expense_tracker")
            .withUsername("test")
            .withPassword("test");

    static {
        System.setProperty("spring.datasource.url", mysql.getJdbcUrl());
        System.setProperty("spring.datasource.username", mysql.getUsername());
        System.setProperty("spring.datasource.password", mysql.getPassword());
        System.setProperty("spring.jpa.hibernate.ddl-auto", "update");
    }

    @Autowired
    TestRestTemplate rest;

    @Test
    void create_update_delete_undo() {
        ExpenseCreateRequest create = new ExpenseCreateRequest(new BigDecimal("12.34"), LocalDate.now(), "Food", "Lunch");
        ExpenseResponse created = rest.postForObject("/api/expenses", create, ExpenseResponse.class);
        assertThat(created).isNotNull();
        assertThat(created.id()).isNotNull();

        ExpenseUpdateRequest update = new ExpenseUpdateRequest(new BigDecimal("20.00"), create.date(), "Food", "Dinner");
        ResponseEntity<ExpenseResponse> updated = rest.exchange(
                "/api/expenses/" + created.id(),
                HttpMethod.PUT,
                new HttpEntity<>(update),
                ExpenseResponse.class
        );
        assertThat(updated.getBody()).isNotNull();
        assertThat(updated.getBody().amount()).isEqualByComparingTo("20.00");

        DeleteResponse del = rest.exchange("/api/expenses/" + created.id(), HttpMethod.DELETE, null, DeleteResponse.class).getBody();
        assertThat(del).isNotNull();
        assertThat(del.deleted()).isTrue();

        ResponseEntity<String> getAfterDelete = rest.getForEntity("/api/expenses/" + created.id(), String.class);
        assertThat(getAfterDelete.getStatusCode().value()).isEqualTo(404);

        ResponseEntity<String> undo = rest.postForEntity("/api/expenses/" + created.id() + "/undo-delete", null, String.class);
        assertThat(undo.getStatusCode().value()).isEqualTo(200);

        ExpenseResponse restored = rest.getForObject("/api/expenses/" + created.id(), ExpenseResponse.class);
        assertThat(restored).isNotNull();
        assertThat(restored.note()).isEqualTo("Dinner");
    }
}
