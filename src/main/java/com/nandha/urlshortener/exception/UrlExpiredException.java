package com.nandha.urlshortener.exception;

/**
 * Thrown when an expired URL is accessed.
 */
public class UrlExpiredException extends RuntimeException {

    public UrlExpiredException(String shortCode) {
        super(
                "URL has expired: "
                        + shortCode
        );
    }
}