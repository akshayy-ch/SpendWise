package com.spendwise.exception.EmailExceptions;

public class EmailVerificationTokenExpiredException extends RuntimeException {
    public EmailVerificationTokenExpiredException(String message) {
        super(message);
    }
}
