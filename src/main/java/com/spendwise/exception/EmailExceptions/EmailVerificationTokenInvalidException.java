package com.spendwise.exception.EmailExceptions;

public class EmailVerificationTokenInvalidException extends RuntimeException {
    public EmailVerificationTokenInvalidException(String message) {
        super(message);
    }
}
