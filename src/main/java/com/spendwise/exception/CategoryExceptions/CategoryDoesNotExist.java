package com.spendwise.exception.CategoryExceptions;

public class CategoryDoesNotExist extends RuntimeException {
    public CategoryDoesNotExist(String message) {
        super(message);
    }
}
