package com.uzum.currencyconverter.exception;

import com.uzum.currencyconverter.utils.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ExceptionHandler {
    @org.springframework.web.bind.annotation.ExceptionHandler({
            NotFoundException.class,
            SecretKeyException.class,
            NotEnoughMoneyException.class,
            OfficialRateFetchException.class
    })
    public ResponseEntity<ApiErrorResponse> handleApiException(RuntimeException ex) {
        HttpStatus status = resolveHttpStatus(ex);
        ApiErrorResponse apiError = new ApiErrorResponse(ex.getMessage(), status.value(), status, LocalDateTime.now());
        return ResponseEntity.status(status).body(apiError);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleException() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
    }

    private HttpStatus resolveHttpStatus(Exception ex) {
        ResponseStatus responseStatus = ex.getClass().getAnnotation(ResponseStatus.class);
        return (responseStatus != null) ? responseStatus.value() : HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
