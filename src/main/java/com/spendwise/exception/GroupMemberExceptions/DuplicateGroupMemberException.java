package com.spendwise.exception.GroupMemberExceptions;

public class DuplicateGroupMemberException extends RuntimeException {
    public DuplicateGroupMemberException(String message) {
        super(message);
    }
}
