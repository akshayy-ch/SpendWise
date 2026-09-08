package com.spendwise.exception.BudgetExceptions;

public class InvalidBudgetLimitException extends RuntimeException {
    public InvalidBudgetLimitException(String message) {
        super(message);
    }
}
