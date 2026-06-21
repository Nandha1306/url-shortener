package com.nandha.urlshortener.controller;

import com.nandha.urlshortener.dto.ShortenRequest;
import com.nandha.urlshortener.dto.ShortenResponse;
import com.nandha.urlshortener.dto.UrlStatsResponse;
import com.nandha.urlshortener.entity.Url;
import com.nandha.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    /**
     * Creates a short URL for the given original URL.
     */
    @PostMapping
    public ResponseEntity<ShortenResponse> shorten(
            @Valid @RequestBody ShortenRequest request
    ) {
        ShortenResponse response = urlService.shorten(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<UrlStatsResponse> stats(
            @PathVariable String shortCode
    ) {

        Url url = urlService.getByShortCode(shortCode);

        return ResponseEntity.ok(
                new UrlStatsResponse(
                        url.getShortCode(),
                        url.getOriginalUrl(),
                        url.getClickCount(),
                        url.getCreatedAt()
                )
        );
    }
}