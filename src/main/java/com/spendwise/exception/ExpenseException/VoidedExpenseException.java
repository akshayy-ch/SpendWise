package com.spendwise.exception.ExpenseException;

public class VoidedExpenseException extends RuntimeException {
    public VoidedExpenseException(String message) {
        super(message);
    }
}
