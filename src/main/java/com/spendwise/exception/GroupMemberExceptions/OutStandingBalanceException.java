package com.spendwise.exception.GroupMemberExceptions;

public class OutStandingBalanceException extends RuntimeException {
    public OutStandingBalanceException(String message) {
        super(message);
    }
}
