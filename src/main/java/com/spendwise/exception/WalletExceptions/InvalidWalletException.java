package com.spendwise.exception.WalletExceptions;

public class InvalidWalletException extends RuntimeException {
    public InvalidWalletException(String message) {
        super(message);
    }
}
