package com.nandha.urlshortener.service;

import com.nandha.urlshortener.dto.ShortenRequest;
import com.nandha.urlshortener.dto.ShortenResponse;
import com.nandha.urlshortener.dto.UrlStatsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * URL business operations.
 */
public interface UrlService {
    ShortenResponse shorten (ShortenRequest request);
    /**
     * Returns the original URL for a short code.
     */
    String resolve(String shortCode);

    UrlStatsResponse getStats(String shortCode);

    /**
     * Deletes a URL using its short code.
     */
    void delete(String shortCode);

    Page<UrlStatsResponse> list(Pageable pageable);
}
