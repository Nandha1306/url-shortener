package com.nandha.urlshortener.service;

import com.nandha.urlshortener.dto.ShortenRequest;
import com.nandha.urlshortener.dto.ShortenResponse;
import com.nandha.urlshortener.dto.CachedUrl;
import com.nandha.urlshortener.dto.UrlStatsResponse;
import com.nandha.urlshortener.entity.Url;
import com.nandha.urlshortener.exception.UrlExpiredException;
import com.nandha.urlshortener.exception.UrlNotFoundException;
import com.nandha.urlshortener.mapper.UrlMapper;
import com.nandha.urlshortener.repository.UrlRepository;
import com.nandha.urlshortener.service.cache.RedisCacheService;
import com.nandha.urlshortener.util.Base62Encoder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

    private final UrlRepository urlRepository;
    private final RedisCacheService cacheService;
    private final AnalyticsService analyticsService;
    private final UrlMapper urlMapper;

    /**
     * Creates a shortened URL.
     *
     * The entire operation is atomic. If any step fails,
     * the transaction is rolled back.
     */
    @Override
    @Transactional
    @Operation(
            summary = "Create a short URL",
            description = "Creates a shortened URL for the given original URL."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Short URL created"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    public ShortenResponse shorten(ShortenRequest request) {
        Url url = Url.builder()
                .originalUrl(request.originalUrl())
                .expiresAt(
                        resolveExpiry(request.expiresInDays())
                )
                .build();

        // saving the url into db.
        Url saved = urlRepository.save(url);
        // get the original url by using the id.
        String shortCode = Base62Encoder.encode(saved.getId());
        saved.setShortCode(shortCode);
        urlRepository.save(saved);

        log.info("Created short URL '{}' for '{}'",
                saved.getShortCode(),
                saved.getOriginalUrl());

        return urlMapper.toShortenResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public String resolve(String shortCode) {
        // check it the shortcode is in cache memory or not.
        CachedUrl cached = cacheService.get(shortCode);

        // if yes means return it from the cache itself
        if(cached != null){
            log.debug("Cache hit for {}", shortCode);

            // if the cached url is expired
            if(cached.expiresAt() != null &&
                    LocalDateTime.now().isAfter(cached.expiresAt())) {
                throw new UrlExpiredException(shortCode);
            }

            // if not expired means return the url
            analyticsService.incrementClickCount(cached.id());
            return cached.originalUrl();
        }

        // if not present in the cache means it query in db
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new UrlNotFoundException(shortCode));

        log.debug("Cache miss for {}", shortCode);

        // storing the queried shortcode into cache memory
        cacheService.cache(
                shortCode,
                new CachedUrl(
                        url.getId(),
                        url.getOriginalUrl(),
                        url.getExpiresAt()
                )
        );
        // increase the counter
        analyticsService.incrementClickCount(url.getId());
        return url.getOriginalUrl();
    }

    /**
     * Returns statistics for a shortened URL.
     */
    @Override
    @Transactional(readOnly = true)
    public UrlStatsResponse getStats(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new UrlNotFoundException(shortCode));

        return urlMapper.toStatsResponse(url);
    }

    /**
     * Deletes a shortened URL.
     */
    @Override
    @Transactional
    public void delete(String shortCode) {
        if (!urlRepository.existsByShortCode(shortCode)) {
            throw new UrlNotFoundException(shortCode);
        }
        urlRepository.deleteByShortCode(shortCode);
        cacheService.evict(shortCode);
        log.info("Deleted short URL '{}'", shortCode);
    }

    /**
     * Returns a paginated list of shortened URLs.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UrlStatsResponse> list(Pageable pageable) {
        return urlRepository.findAll(pageable)
                .map(urlMapper::toStatsResponse);
    }

    /**
     * Converts expiry days into a timestamp.
     */

    private LocalDateTime resolveExpiry( Integer expiresInDays) {
        if (expiresInDays == null) {
            return null;
        }

        return LocalDateTime.now()
                .plusDays(expiresInDays);
    }
}
