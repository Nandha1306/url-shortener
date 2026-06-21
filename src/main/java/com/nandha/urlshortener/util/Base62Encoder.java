package com.nandha.urlshortener.util;

/**
 * Converts numeric IDs into URL-safe Base62 strings.
 *
 * Example:
 * 125 -> "21"
 * 1000 -> "G8"
 */

public final class Base62Encoder {
    private static final String ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static final int BASE = 62;

    private Base62Encoder(){}

    public static String encode(long value){
        if (value < 0) {
            throw new IllegalArgumentException("Value cannot be negative");
        }

        if(value == 0) return "0";

        StringBuilder encoded = new StringBuilder();

        while(value > 0) {
            encoded.append(ALPHABET.charAt((int)(value % BASE)));
            value /= BASE;
        }

        return encoded.reverse().toString();
    }
}
