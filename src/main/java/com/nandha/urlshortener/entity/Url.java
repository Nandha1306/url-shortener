package com.nandha.urlshortener.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a URL mapping stored in the system.
 *
 * Example:
 * https://spring.io
 *      ↓
 * aB3D7kP
 */

@Entity
@Table(name = "urls")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Url {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_url", nullable = false)
    private String originalUrl;

    @Column(name = "short_code", nullable = true)
    private String shortCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Whenever Hibernate inserts a new row, this method runs automatically.
    @PrePersist
    void onCreate(){
        createdAt = LocalDateTime.now();
    }

    /**
     * Total number of successful redirects.
     */
    @Column(name = "click_count", nullable = false)
    @Builder.Default
    private Long clickCount = 0L;

    /**
     * Optional expiration timestamp.
     *
     * If current time passes this value,
     * redirects are no longer allowed.
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * Checks whether the URL has expired.
     */
    public boolean isExpired() {
        return expiresAt != null
                && LocalDateTime.now()
                .isAfter(expiresAt);
    }
}
