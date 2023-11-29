package com.uzum.currencyconverter.exception;

public class OfficialRateFetchException extends RuntimeException {
    public OfficialRateFetchException(String message) {
        super(message);
    }
}
