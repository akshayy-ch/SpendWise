package com.spendwise.exception.AuthExceptions;

public class PhoneNoAlreadyExistsException extends RuntimeException{
    public PhoneNoAlreadyExistsException(String message){
        super(message);
    }
}
