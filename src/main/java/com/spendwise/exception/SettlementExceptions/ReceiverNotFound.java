package com.spendwise.exception.SettlementExceptions;

public class ReceiverNotFound extends RuntimeException {
    public ReceiverNotFound(String message) {
        super(message);
    }
}
