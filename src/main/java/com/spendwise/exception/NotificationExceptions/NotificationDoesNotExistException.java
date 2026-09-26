package com.spendwise.exception.NotificationExceptions;

public class NotificationDoesNotExistException extends RuntimeException {
    public NotificationDoesNotExistException(String message) {
        super(message);
    }
}
