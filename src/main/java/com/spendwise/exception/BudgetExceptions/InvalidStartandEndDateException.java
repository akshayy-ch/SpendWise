package com.spendwise.exception.BudgetExceptions;

public class InvalidStartandEndDateException extends RuntimeException {
    public InvalidStartandEndDateException(String message) {
        super(message);
    }
}
