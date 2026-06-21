package com.nandha.urlshortener.controller;

import com.nandha.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class RedirectController {
    private final UrlService urlService;

    /**
     * Redirects the user to the original URL.
     */
    @GetMapping("/r/{shortCode}")
    public ResponseEntity<Void> redirect(
            @Valid @PathVariable String shortCode
    ) {
        String originalUrl = urlService.resolve(shortCode);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(originalUrl)) // helps to redirect
                .build();
    }
}
