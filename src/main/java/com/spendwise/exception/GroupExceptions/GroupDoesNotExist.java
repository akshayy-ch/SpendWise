package com.spendwise.exception.GroupExceptions;

public class GroupDoesNotExist extends RuntimeException {

    public GroupDoesNotExist(String message) {
        super(message);
    }
}