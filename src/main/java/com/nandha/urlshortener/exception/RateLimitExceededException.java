package com.nandha.urlshortener.exception;
/**
 * Thrown when a client exceeds
 * the allowed request rate.
 */
public class RateLimitExceededException extends RuntimeException{
    public RateLimitExceededException() {
        super("Too many requests. Try again later.");
    }
}
