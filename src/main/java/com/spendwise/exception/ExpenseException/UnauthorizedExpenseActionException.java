package com.spendwise.exception.ExpenseException;

public class UnauthorizedExpenseActionException extends RuntimeException {
    public UnauthorizedExpenseActionException(String message) {
        super(message);
    }
}
