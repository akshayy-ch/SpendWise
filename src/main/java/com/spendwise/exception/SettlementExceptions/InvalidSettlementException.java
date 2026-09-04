package com.spendwise.exception.SettlementExceptions;

public class InvalidSettlementException extends RuntimeException {
    public InvalidSettlementException(String message) {
        super(message);
    }
}
