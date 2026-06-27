package com.nandha.urlshortener.exception;

import org.springframework.beans.factory.parsing.Problem;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Converts exceptions into HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Handles missing short code errors.
     */
    @ExceptionHandler(UrlNotFoundException.class)
    public ProblemDetail handleUrlNotFound(UrlNotFoundException ex) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.NOT_FOUND,
                        ex.getMessage()
                );
        problemDetail.setTitle("URL Not Found");
        return problemDetail;
    }

    /**
     * Handles request validation failures.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        message
                );

        problemDetail.setTitle("Validation Failed");
        return problemDetail;
    }

    /**
     * Handles rate limit violations.
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ProblemDetail handleRateLimit(RateLimitExceededException ex) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.TOO_MANY_REQUESTS,
                        ex.getMessage()
                );

        problemDetail.setTitle("Rate Limit Exceeded");
        return problemDetail;
    }

    @ExceptionHandler(UrlExpiredException.class)
    public ProblemDetail handleExpired(UrlExpiredException ex) {
        ProblemDetail problemDetail = ProblemDetail
                .forStatusAndDetail(
                        HttpStatus.GONE,
                        ex.getMessage()
                );

        problemDetail.setTitle("URL Expired");
        return problemDetail;
    }
}
