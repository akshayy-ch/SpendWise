package com.spendwise.exception.EmailExceptions;

public class EmailNotVerifiedException extends RuntimeException     {
    public EmailNotVerifiedException(String message) {
        super(message);
    }
}
