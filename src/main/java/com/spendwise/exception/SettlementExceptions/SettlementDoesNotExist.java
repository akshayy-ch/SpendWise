package com.spendwise.exception.SettlementExceptions;

public class SettlementDoesNotExist extends RuntimeException {

    public SettlementDoesNotExist(String message) {
        super(message);
    }
}