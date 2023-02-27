package io.rocketbase.commons.util;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

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
     * return false when value is null
     */
    public static Boolean notNull(Boolean value) {
        if (value == null) {
            return false;
        }
        return value;
    }

    /**
     * return 0 when value is null
     */
    public static Long notNull(Long value) {
        if (value == null) {
            return 0l;
        }
        return value;
    }

    /**
     * return BigDecimal.ZERO when value is null
     */
    public static BigDecimal notNull(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value;
    }

    /**
     * in case of null value return's new ArrayList
     */
    public static <T> List<T> notNull(List<T> value) {
        if (value == null) {
            return new ArrayList<>();
        }
        return value;
    }

    /**
     * in case of null value return's new HashSet
     */
    public static <T> Set<T> notNull(Set<T> value) {
        if (value == null) {
            return new HashSet<>();
        }
        return value;
    }

    /**
     * in case of null value return's new HashMap<K,V
     */
    public static <K, V> Map<K, V> notNull(Map<K, V> value) {
        if (value == null) {
            return new HashMap<>();
        }
        return value;
    }

    /**
     * accept when value is not null
     */
    public static <T> void accept(T value, Consumer<T> consumer) {
        if (value != null) {
            consumer.accept(value);
        }
    }

    /**
     * apply when value is not null
     */
    public static <T, R> R apply(T value, Function<T, R> function, R fallback) {
        if (value != null) {
            return function.apply(value);
        }
        return fallback;
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

    public static <SOURCE> String notNull(SOURCE value, PropertyValueProvider<SOURCE, String> provider) {
        return notNull(value, provider, "");
    }

    public static <SOURCE> String notEmpty(SOURCE value, PropertyValueProvider<SOURCE, String> provider, String fallback) {
        if (value == null) {
            return fallback;
        }
        String result = provider.apply(value);
        return notEmpty(result, fallback);
    }

    /**
     * return's true if all values are not null<br>
     * false in case one is null
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> boolean noneNullValue(T... values) {
        for (T v : values) {
            if (v == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * return's true if at least one of the values it not null
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> boolean anyNoneNullValue(T... values) {
        for (T v : values) {
            if (v != null) {
                return true;
            }
        }
        return false;
    }

    public interface PropertyValueProvider<SOURCE, TARGET> {
        TARGET apply(SOURCE source);
    }

}
