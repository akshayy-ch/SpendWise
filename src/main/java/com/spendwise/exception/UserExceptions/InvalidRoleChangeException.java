package com.spendwise.exception.UserExceptions;

public class InvalidRoleChangeException extends RuntimeException {
    public InvalidRoleChangeException(String message) {
        super(message);
    }
}
