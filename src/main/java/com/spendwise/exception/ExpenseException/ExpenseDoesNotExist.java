package com.spendwise.exception.ExpenseException;

public class ExpenseDoesNotExist extends RuntimeException {
    public ExpenseDoesNotExist(String message) {
        super(message);
    }
}
