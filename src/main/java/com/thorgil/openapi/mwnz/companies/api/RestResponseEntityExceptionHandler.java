package com.thorgil.openapi.mwnz.companies.api;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.thorgil.openapi.mwnz.companies.model.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.http.HttpHeaders;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestResponseEntityExceptionHandler.class);
    private static final String COMPANY_NOT_FOUND_MESSAGE = "The requested company was not found";
    private static final String UNEXPECTED_ERROR_MESSAGE = "An unexpected error occurred";

    public RestResponseEntityExceptionHandler() {
        super();
    }

    @ExceptionHandler(value = { HttpClientErrorException.class })
    protected ResponseEntity<Object> handleHttpClientErrorException(HttpClientErrorException hcee, WebRequest request) {
        Error error = new Error();
        HttpStatusCode status = hcee.getStatusCode();

        if (status == NOT_FOUND) {
            error.setError(NOT_FOUND.toString());
            error.setErrorDescription(COMPANY_NOT_FOUND_MESSAGE);
        } else {
            error.setError(status.toString());
            error.setErrorDescription(UNEXPECTED_ERROR_MESSAGE);
        }

        logger.error("HttpClientErrorException: {}", hcee.getMessage(), hcee);

        return handleExceptionInternal(hcee, error, new HttpHeaders(), status, request);
    }
}

