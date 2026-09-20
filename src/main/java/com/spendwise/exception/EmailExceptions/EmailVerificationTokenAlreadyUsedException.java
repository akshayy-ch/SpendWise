package com.spendwise.exception.EmailExceptions;

public class EmailVerificationTokenAlreadyUsedException extends RuntimeException {
    public EmailVerificationTokenAlreadyUsedException(String message) {
        super(message);
    }
}
