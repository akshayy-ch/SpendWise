package com.spendwise.exception.ExpenseException;

public class InvalidExpenseSortFieldException extends RuntimeException {
    public InvalidExpenseSortFieldException(String message) {
        super(message);
    }
}
