package com.nandha.urlshortener.service;

import com.nandha.urlshortener.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final UrlRepository urlRepository;

    /**
     * Increments click count without blocking
     * the redirect response.
     */
    @Async
    @Transactional
    public void incrementClickCount(Long urlId) {
        urlRepository.incrementClickCount(urlId);
    }
}
