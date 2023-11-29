package com.uzum.currencyconverter.exception;

public class InvalidPairException extends IllegalArgumentException {
    public InvalidPairException(String message) {
        super(message);
    }
}
