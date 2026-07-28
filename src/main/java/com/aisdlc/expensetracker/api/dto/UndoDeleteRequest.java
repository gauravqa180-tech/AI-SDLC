package com.aisdlc.expensetracker.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UndoDeleteRequest(
        @NotBlank String undoToken
) {
}
