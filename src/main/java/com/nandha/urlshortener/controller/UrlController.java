package com.nandha.urlshortener.controller;

import com.nandha.urlshortener.dto.ShortenRequest;
import com.nandha.urlshortener.dto.ShortenResponse;
import com.nandha.urlshortener.dto.UrlStatsResponse;
import com.nandha.urlshortener.entity.Url;
import com.nandha.urlshortener.service.RateLimiterService;
import com.nandha.urlshortener.service.UrlService;
import com.nandha.urlshortener.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes URL shortening APIs.
 *
 * Responsibilities:
 * - Accept HTTP requests
 * - Delegate to service layer
 * - Return HTTP responses
 *
 * No business logic should live here.
 */
@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;
    private final RateLimiterService rateLimiterService;

    /**
     * Creates a short URL for the given original URL.
     */
    @PostMapping
    public ResponseEntity<ShortenResponse> shorten(
            @Valid @RequestBody ShortenRequest request,
            HttpServletRequest  httpRequest
    ) {
        String clientIp =
                IpUtil.extractClientIp(httpRequest);

        rateLimiterService.checkLimit(clientIp);

        ShortenResponse response = urlService.shorten(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Returns metadata and analytics for a short URL.
     */
    @GetMapping("/{shortCode}")
    public ResponseEntity<UrlStatsResponse> stats(
            @PathVariable String shortCode
    ) {
        return ResponseEntity.ok(
                urlService.getStats(shortCode)
        );
    }

    /**
     * Deletes a shortened URL.
     */
    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> delete(@PathVariable String shortCode) {
        urlService.delete(shortCode);
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns a paginated list of shortened URLs.
     */
    @GetMapping
    public ResponseEntity<Page<UrlStatsResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(
                urlService.list(pageable)
        );
    }
}