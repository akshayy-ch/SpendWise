package com.spendwise.exception.GroupMemberExceptions;

public class NotGroupMemberException extends RuntimeException {
    public NotGroupMemberException(String message) {
        super(message);
    }
}
