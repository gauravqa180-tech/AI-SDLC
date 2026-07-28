package com.aisdlc.expensetracker.web.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UndoTokenInvalidException extends RuntimeException {
    public UndoTokenInvalidException(String message) {
        super(message);
    }
}
