package com.uzum.currencyconverter.exception;

public class NoCurrencyDataException extends RuntimeException {
    public NoCurrencyDataException(String message) {
        super(message);
    }
}
