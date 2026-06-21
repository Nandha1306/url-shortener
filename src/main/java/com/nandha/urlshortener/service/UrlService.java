package com.nandha.urlshortener.service;

import com.nandha.urlshortener.dto.ShortenRequest;
import com.nandha.urlshortener.dto.ShortenResponse;
import com.nandha.urlshortener.entity.Url;

/**
 * URL business operations.
 */
public interface UrlService {
    ShortenResponse shorten (ShortenRequest request);
    /**
     * Returns the original URL for a short code.
     */
    String resolve(String shortCode);

    Url getByShortCode(String shortCode);
}
