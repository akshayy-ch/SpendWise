package com.spendwise.exception.BudgetExceptions;

public class BudgetAlreadyExistsException extends RuntimeException {
    public BudgetAlreadyExistsException(String message) {
        super(message);
    }
}
