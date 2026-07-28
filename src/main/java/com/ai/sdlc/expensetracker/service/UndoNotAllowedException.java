package com.ai.sdlc.expensetracker.service;

public class UndoNotAllowedException extends RuntimeException {
    public UndoNotAllowedException(String message) {
        super(message);
    }
}
