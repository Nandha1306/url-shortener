package com.nandha.urlshortener.service;

import com.nandha.urlshortener.dto.ShortenRequest;
import com.nandha.urlshortener.dto.ShortenResponse;
import com.nandha.urlshortener.entity.CachedUrl;
import com.nandha.urlshortener.entity.Url;
import com.nandha.urlshortener.exception.UrlNotFoundException;
import com.nandha.urlshortener.repository.UrlRepository;
import com.nandha.urlshortener.service.cache.RedisCacheService;
import com.nandha.urlshortener.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

    private final UrlRepository urlRepository;
    private final RedisCacheService cacheService;
    private final AnalyticsService analyticsService;

    @Override
    public ShortenResponse shorten(ShortenRequest request) {
        Url url = Url.builder()
                .originalUrl(request.originalUrl())
                .build();

        // saving the url into db.
        Url saved = urlRepository.save(url);
        // get the original url by using the id.
        String shortCode = Base62Encoder.encode(saved.getId());
        saved.setShortCode(shortCode);
        urlRepository.save(saved);

        return new ShortenResponse(
                shortCode,
                "/r/" + shortCode,
                saved.getOriginalUrl()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public String resolve(String shortCode) {
        // check it the shortcode is in cache memory or not.
        CachedUrl cached = cacheService.get(shortCode);

        // if yes means return it from the cache itself
        if(cached != null){
//            System.out.println("CACHE HIT");
            analyticsService.incrementClickCount(cached.id());
            return cached.originalUrl();
        }

        // if not present in the cache means it query in db
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new UrlNotFoundException(shortCode));

//        System.out.println("CACHE MISS");
        // storing the queried shortcode into cache memory
        cacheService.cache(
                shortCode,
                new CachedUrl(
                        url.getId(),
                        url.getOriginalUrl()
                )
        );
        // increase the counter
        analyticsService.incrementClickCount(url.getId());
        return url.getOriginalUrl();
    }

    @Override
    @Transactional(readOnly = true)
    public Url getByShortCode(String shortCode) {

        return urlRepository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new UrlNotFoundException(shortCode));
    }
}
