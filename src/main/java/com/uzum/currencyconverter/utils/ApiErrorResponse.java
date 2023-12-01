package com.uzum.currencyconverter.utils;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@NoArgsConstructor
public class ApiErrorResponse extends Response {
    public ApiErrorResponse(String message, int code, HttpStatus status, LocalDateTime timestamp) {
        super(message, code, status, timestamp);
    }
}
