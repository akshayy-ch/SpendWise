package com.spendwise.exception.BudgetExceptions;

public class DuplicateBudgetCategoryException extends RuntimeException {
    public DuplicateBudgetCategoryException(String message) {
        super(message);
    }
}
