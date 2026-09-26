package com.spendwise.exception.SettlementExceptions;

public class InvalidSettlementSortFieldException extends RuntimeException {
    public InvalidSettlementSortFieldException(String message) {
        super(message);
    }
}
