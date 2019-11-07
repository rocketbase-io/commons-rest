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

    public static <TARGET, SOURCE> TARGET notNull(SOURCE value, PropertyValueProvider<SOURCE, TARGET> provider, TARGET fallback) {
        if (value == null) {
            return fallback;
        }
        TARGET result = provider.apply(value);
        return notNull(result, fallback);
    }

    public static <SOURCE> String notEmpty(SOURCE value, PropertyValueProvider<SOURCE, String> provider, String fallback) {
        if (value == null) {
            return fallback;
        }
        String result = provider.apply(value);
        return notEmpty(result, fallback);
    }

    public static <T> boolean noneNullValue(T... values) {
        for (T v : values) {
            if (v == null) {
                return false;
            }
        }
        return true;
    }

    public interface PropertyValueProvider<SOURCE, TARGET> {
        TARGET apply(SOURCE source);
    }

}
