package com.nandha.urlshortener;

import com.nandha.urlshortener.dto.CachedUrl;
import com.nandha.urlshortener.dto.ShortenRequest;
import com.nandha.urlshortener.dto.ShortenResponse;
import com.nandha.urlshortener.entity.Url;
import com.nandha.urlshortener.exception.UrlNotFoundException;
import com.nandha.urlshortener.mapper.UrlMapper;
import com.nandha.urlshortener.repository.UrlRepository;
import com.nandha.urlshortener.service.AnalyticsService;
import com.nandha.urlshortener.service.UrlServiceImpl;
import com.nandha.urlshortener.service.cache.RedisCacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UrlServiceImpl.
 *
 * Tests only the business logic by mocking
 * all external dependencies.
 *
 * Mockito Reference:
 * @Mock                     -> Creates fake dependency
 * @InjectMocks              -> Creates object under test
 * when(...).thenReturn(...) -> Stub behavior
 * verify(...)               -> Verify method invocation
 * verifyNoInteractions(...) -> Ensure dependency wasn't used
 * times(n)                  -> Expected invocation count
 * any()                     -> Match any object
 * eq()                      -> Match exact value
 * assertThrows()            -> Verify exceptions
 */
@ExtendWith(MockitoExtension.class)
class UrlServiceImplTest {
    @Mock
    private UrlRepository urlRepository;
    @Mock
    private RedisCacheService cacheService;
    @Mock
    private AnalyticsService analyticsService;
    @Mock
    private UrlMapper urlMapper;
    @InjectMocks
    private UrlServiceImpl urlService;

    @Test
    void shouldCreateShortUrl() {
        ShortenRequest request = new ShortenRequest(
                "https://spring.io",
                null
        );

        Url saved = Url.builder()
                .id(1L)
                .originalUrl(request.originalUrl())
                .build();

        when(urlRepository.save(any(Url.class)))
                .thenReturn(saved);

        when(urlMapper.toShortenResponse(any(Url.class)))
                .thenReturn(
                        new ShortenResponse(
                                "1",
                                "/r/1",
                                request.originalUrl(),
                                LocalDateTime.now(),
                                null
                        )
                );

        ShortenResponse response = urlService.shorten(request);

        assertEquals("1", response.shortCode());

        // Change to times(1) if your service only saves once.
        verify(urlRepository, times(2))
                .save(any(Url.class));

        verify(urlMapper)
                .toShortenResponse(any(Url.class));
    }

    @Test
    void shouldReturnCachedUrlWhenCacheHit() {
        CachedUrl cached = new CachedUrl(
                1L,
                "https://spring.io",
                null
        );

        when(cacheService.get("abc123"))
                .thenReturn(cached);

        String result = urlService.resolve("abc123");

        assertEquals("https://spring.io", result);

        verify(cacheService).get("abc123");

        verifyNoInteractions(urlRepository);
    }

    @Test
    void shouldQueryDatabaseWhenCacheMiss() {
        when(cacheService.get("abc123"))
                .thenReturn(null);

        Url url = Url.builder()
                .id(1L)
                .shortCode("abc123")
                .originalUrl("https://spring.io")
                .build();

        when(urlRepository.findByShortCode("abc123"))
                .thenReturn(Optional.of(url));

        String result = urlService.resolve("abc123");

        assertEquals("https://spring.io", result);

        verify(cacheService)
                .cache(
                        eq("abc123"),
                        any(CachedUrl.class)
                );
    }

    @Test
    void shouldThrowExceptionWhenShortCodeDoesNotExist() {
        when(cacheService.get(any(String.class)))
                .thenReturn(null);

        when(urlRepository.findByShortCode(any(String.class)))
                .thenReturn(Optional.empty());

        assertThrows(
                UrlNotFoundException.class,
                () -> urlService.resolve("unknown")
        );
    }
}