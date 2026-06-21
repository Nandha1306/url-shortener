package com.nandha.urlshortener.exception;

/**
 * Thrown when a short code does not exist.
 */
public class UrlNotFoundException extends RuntimeException{
    public UrlNotFoundException(String shortCode) {
        super("No URL found for short code: " + shortCode);
    }
}
