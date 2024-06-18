package com.thorgil.openapi.mwnz.companies.api;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.thorgil.openapi.mwnz.companies.model.Error;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.http.HttpHeaders;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    public RestResponseEntityExceptionHandler() {
        super();
    }
    @ExceptionHandler(value = { HttpClientErrorException.class})
    protected ResponseEntity<Object> handleNotFound(HttpClientErrorException hcee, WebRequest request) {
        if (hcee.getStatusCode() == NOT_FOUND) {
            Error error = new Error();
            error.setError(NOT_FOUND.toString());
            error.setErrorDescription("The requested company was not found");
            return handleExceptionInternal(hcee, error, new HttpHeaders(), NOT_FOUND, request);
        } else {
            Error error = new Error();
            error.setError(hcee.getStatusCode().toString());
            error.setErrorDescription("An unexpected error occurred");
            return handleExceptionInternal(hcee, error, new HttpHeaders(), hcee.getStatusCode(), request);
        }
    }
}
