package com.nandha.urlshortener.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Extracts the real client IP.
 */
public final class IpUtil {

    private IpUtil() {}

    public static String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");

        // The first IP in the comma-separated list is the original client
        if(forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
