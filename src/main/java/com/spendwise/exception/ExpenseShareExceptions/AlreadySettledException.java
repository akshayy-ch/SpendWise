package com.spendwise.exception.ExpenseShareExceptions;

public class AlreadySettledException extends RuntimeException {
    public AlreadySettledException(String message) {
        super(message);
    }
}
