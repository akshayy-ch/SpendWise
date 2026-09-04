package com.spendwise.exception.WalletExceptions;

public class WalletDoesNotExist extends RuntimeException {
    public WalletDoesNotExist(String message) {
        super(message);
    }
}
