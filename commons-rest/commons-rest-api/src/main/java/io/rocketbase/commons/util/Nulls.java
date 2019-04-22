package io.rocketbase.commons.util;

/**
 * shorten null checks
 */
public final class Nulls {

    /**
     * when value is null returns fallback
     */
    public static <T> T notNull(T value, T fallback) {
        if (value == null) {
            return fallback;
        }
        return value;
    }

    /**
     * returns empty string when value is null
     */
    public static String notNull(String value) {
        return notNull(value, "");
    }

    /**
     * returns fallback when value is empty
     */
    public static String notEmpty(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value;
    }
}
