package com.spendwise.exception.GroupExceptions;

public class UnauthorizedGroupActionException extends RuntimeException {
    public UnauthorizedGroupActionException(String message) {
        super(message);
    }
}
