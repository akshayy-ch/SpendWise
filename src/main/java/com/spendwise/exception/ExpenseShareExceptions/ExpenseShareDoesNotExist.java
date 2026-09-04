package com.spendwise.exception.ExpenseShareExceptions;

public class ExpenseShareDoesNotExist extends RuntimeException {
    public ExpenseShareDoesNotExist(String message) {
        super(message);
    }
}
