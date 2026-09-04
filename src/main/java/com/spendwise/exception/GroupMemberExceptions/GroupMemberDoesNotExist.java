package com.spendwise.exception.GroupMemberExceptions;

public class GroupMemberDoesNotExist extends RuntimeException {
    public GroupMemberDoesNotExist(String message) {
        super(message);
    }
}
