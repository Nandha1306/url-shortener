package com.nandha.urlshortener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nandha.urlshortener.dto.ShortenRequest;
import com.nandha.urlshortener.entity.Url;
import com.nandha.urlshortener.repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for URL APIs.
 *
 * Tests the complete request flow:
 * HTTP -> Controller -> Service -> Repository -> Database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UrlControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UrlRepository urlRepository;

    @BeforeEach
    void setup() {
        urlRepository.deleteAll();
    }

    /**
     * Should create a shortened URL.
     */
    @Test
    void shouldCreateShortUrl() throws Exception {

        ShortenRequest request =
                new ShortenRequest(
                        "https://spring.io",
                        null
                );

        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").exists())
                .andExpect(jsonPath("$.originalUrl")
                        .value("https://spring.io"));
    }

    /**
     * Should reject invalid URL requests.
     */
    @Test
    void shouldReturnBadRequestForInvalidUrl() throws Exception {

        ShortenRequest request =
                new ShortenRequest(
                        "",
                        null
                );

        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest());
    }

    /**
     * Should redirect to the original URL.
     */
    @Test
    void shouldRedirectToOriginalUrl() throws Exception {

        Url url = Url.builder()
                .shortCode("abc123")
                .originalUrl("https://spring.io")
                .clickCount(0L)
                .build();

        urlRepository.save(url);

        mockMvc.perform(get("/r/abc123"))

                .andExpect(status().isFound())
                .andExpect(redirectedUrl("https://spring.io"));
    }

    /**
     * Should return 404 when short code does not exist.
     */
    @Test
    void shouldReturn404ForUnknownShortCode() throws Exception {

        mockMvc.perform(get("/r/unknown"))

                .andExpect(status().isNotFound());
    }

    /**
     * Should return 410 when URL is expired.
     */
    @Test
    void shouldReturnGoneForExpiredUrl() throws Exception {

        Url url = Url.builder()
                .shortCode("expired")
                .originalUrl("https://spring.io")
                .expiresAt(LocalDateTime.now().minusDays(1))
                .clickCount(0L)
                .build();

        urlRepository.save(url);

        mockMvc.perform(get("/r/expired"))

                .andExpect(status().isGone());
    }

    /**
     * Should return URL statistics.
     */
    @Test
    void shouldReturnUrlStats() throws Exception {

        Url url = Url.builder()
                .shortCode("stats")
                .originalUrl("https://spring.io")
                .clickCount(25L)
                .build();

        urlRepository.save(url);

        mockMvc.perform(get("/api/urls/stats"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode")
                        .value("stats"))
                .andExpect(jsonPath("$.clickCount")
                        .value(25));
    }

    /**
     * Should delete a shortened URL.
     */
    @Test
    void shouldDeleteUrl() throws Exception {

        Url url = Url.builder()
                .shortCode("delete")
                .originalUrl("https://spring.io")
                .clickCount(0L)
                .build();

        urlRepository.save(url);

        mockMvc.perform(delete("/api/urls/delete"))

                .andExpect(status().isNoContent());

        mockMvc.perform(get("/r/delete"))

                .andExpect(status().isNotFound());
    }

    /**
     * Should return paginated URLs.
     */
    @Test
    void shouldReturnPaginatedUrls() throws Exception {

        for (int i = 0; i < 5; i++) {

            Url url = Url.builder()
                    .shortCode("code" + i)
                    .originalUrl("https://spring.io/" + i)
                    .clickCount(0L)
                    .build();

            urlRepository.save(url);
        }

        mockMvc.perform(get("/api/urls")
                        .param("page", "0")
                        .param("size", "3"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements")
                        .value(5))
                .andExpect(jsonPath("$.totalPages")
                        .value(2));
    }

    /**
     * Should reject requests after exceeding the rate limit.
     */
    @Test
    void shouldReturnTooManyRequestsAfterRateLimitExceeded() throws Exception {

        ShortenRequest request =
                new ShortenRequest(
                        "https://spring.io",
                        null
                );

        for (int i = 0; i < 3; i++) {

            mockMvc.perform(post("/api/urls")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))

                    .andExpect(status().isCreated());
        }

        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isTooManyRequests());
    }
}