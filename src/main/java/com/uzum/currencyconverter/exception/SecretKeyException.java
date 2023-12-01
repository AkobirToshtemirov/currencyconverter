package com.uzum.currencyconverter.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class SecretKeyException extends RuntimeException {
    public SecretKeyException(String message) {
        super(message);
    }
}
