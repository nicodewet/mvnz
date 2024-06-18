package com.thorgil.openapi.mwnz.companies.api;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.thorgil.openapi.mwnz.companies.model.Error;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private static final String COMPANY_NOT_FOUND_MESSAGE = "The company with id {0} was not found";
    private static final String UNEXPECTED_ERROR_MESSAGE = "An unexpected error occurred";
    private static final Pattern COMPANY_ID_PATTERN = Pattern.compile("/companies/(\\w+)");

    public RestResponseEntityExceptionHandler() {
        super();
    }

    @ExceptionHandler(value = { HttpClientErrorException.class })
    protected ResponseEntity<Object> handleHttpClientErrorException(HttpClientErrorException hcee, WebRequest request) {
        Error error = new Error();
        HttpStatusCode status = hcee.getStatusCode();
        String pathParameter = extractPathParameter(request, COMPANY_ID_PATTERN);

        if (status == NOT_FOUND) {
            error.setError(NOT_FOUND.name());
            error.setErrorDescription(MessageFormat.format(COMPANY_NOT_FOUND_MESSAGE, pathParameter));
        } else {
            error.setError(status.toString());
            error.setErrorDescription(UNEXPECTED_ERROR_MESSAGE);
        }

        logger.error("HttpClientErrorException: {}", hcee.getMessage(), hcee);

        return handleExceptionInternal(hcee, error, new HttpHeaders(), status, request);
    }

    /**
     * Extracts the path parameter from the request URI using the given regex pattern.
     *
     * @param request The current web request.
     * @param pattern The regex pattern to extract the path parameter.
     * @return The extracted path parameter or null if not found.
     */
    private String extractPathParameter(WebRequest request, Pattern pattern) {
        String uri = request.getDescription(false);
        Matcher matcher = pattern.matcher(uri);
        return matcher.find() ? matcher.group(1) : null;
    }
}

