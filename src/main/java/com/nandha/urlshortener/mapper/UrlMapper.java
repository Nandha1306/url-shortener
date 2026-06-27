package com.nandha.urlshortener.mapper;

import com.nandha.urlshortener.dto.ShortenResponse;
import com.nandha.urlshortener.dto.UrlStatsResponse;
import com.nandha.urlshortener.entity.Url;
import org.springframework.stereotype.Component;

/**
 * Maps Url entities to API response DTOs.
 * Keeps mapping logic out of the service layer.
 */
@Component
public class UrlMapper {
    /**
     * Converts a Url entity to a ShortenResponse.
     */
    public ShortenResponse toShortenResponse(Url url) {
        return new ShortenResponse(
                url.getShortCode(),
                "/r/" + url.getShortCode(),
                url.getOriginalUrl(),
                url.getCreatedAt(),
                url.getExpiresAt()
        );
    }
    /**
     * Converts a Url entity to a UrlStatsResponse.
     */
    public UrlStatsResponse toStatsResponse(Url url) {
        return new UrlStatsResponse(
                url.getShortCode(),
                url.getOriginalUrl(),
                url.getClickCount(),
                url.getCreatedAt(),
                url.getExpiresAt()
        );
    }
}
