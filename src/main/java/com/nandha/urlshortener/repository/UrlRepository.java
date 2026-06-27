package com.nandha.urlshortener.repository;

import com.nandha.urlshortener.entity.Url;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {
    Optional<Url> findByShortCode(String shortCode);
    /**
     * Increments click count by one.
     *
     * Uses a direct SQL update instead of loading and saving
     * the entity for better performance.
     */
    @Modifying
    @Query("""
       UPDATE Url u
       SET u.clickCount = u.clickCount + 1
       WHERE u.id = :id
       """)
    void incrementClickCount(Long id);

    /**
     * Deletes a URL using its short code.
     */
    void deleteByShortCode(String shortCode);

    /**
     * Checks whether a short code exists.
     */
    boolean existsByShortCode(String shortCode);

    Page<Url> findAll(Pageable pageable);
}
