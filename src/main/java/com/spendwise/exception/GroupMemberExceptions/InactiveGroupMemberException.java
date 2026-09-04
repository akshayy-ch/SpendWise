package com.spendwise.exception.GroupMemberExceptions;

public class InactiveGroupMemberException extends RuntimeException {
    public InactiveGroupMemberException(String message) {
        super(message);
    }
}
