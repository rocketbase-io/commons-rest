package io.rocketbase.commons.util;

import org.springframework.util.StringUtils;

import java.util.AbstractMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public abstract class LocaleFilter {

    /**
     * see {@link #findClosest(Locale, Map, Locale)}
     */
    public static <T> Optional<Entry<Locale, T>> findClosestOptional(Locale filter, Map<Locale, T> values) {
        return Optional.ofNullable(findClosest(filter, values));
    }

    /**
     * see {@link #findClosest(Locale, Map, Locale)}
     */
    public static <T> Entry<Locale, T> findClosest(Locale filter, Map<Locale, T> values) {
        return findClosest(filter, values, null);
    }

    /**
     * see {@link #findClosest(Locale, Map, Locale)}
     */
    public static <T> Optional<Entry<Locale, T>> findClosestOptional(Locale filter, Map<Locale, T> values, Locale fallback) {
        return Optional.ofNullable(findClosest(filter, values, fallback));
    }

    /**
     * search within values for given locale and follows some rules to try to get the best fitting value
     *
     * @param filter   locale you are searching for <br>
     *                 when containing also country information - searches when no exact match found only by language
     * @param values   map of Locale values
     * @param fallback in case nothing found (filter as it is / just language) try to search again via fallback
     * @return entry with picked locale (in case it has been internally changed) and value of map
     */
    public static <T> Entry<Locale, T> findClosest(Locale filter, Map<Locale, T> values, Locale fallback) {
        if (filter != null && values != null) {
            if (values.containsKey(filter)) {
                // exact match
                return new AbstractMap.SimpleEntry<>(filter, values.get(filter));
            }
            if (!StringUtils.isEmpty(filter.getLanguage())) {
                Locale language = Locale.forLanguageTag(filter.getLanguage());

                if (!StringUtils.isEmpty(filter.getCountry())) {
                    if (values.containsKey(language)) {
                        // match by language - map contains value without country but same language
                        return new AbstractMap.SimpleEntry<>(language, values.get(language));
                    }
                }

                // transform map -> only language keys and find by given language
                Optional<Entry<Locale, Locale>> optional = transformMapFilterByLanguage(values, language);
                if (optional.isPresent()) {
                    return new AbstractMap.SimpleEntry<>(optional.get().getKey(), values.get(optional.get().getKey()));
                }
            }
        }
        if (values != null && fallback != null) {
            return findClosest(fallback, values, !fallback.equals(Locale.ROOT) ? Locale.ROOT : null);
        }
        return null;
    }

    protected static Optional<Entry<Locale, Locale>> transformMapFilterByLanguage(Map<Locale, ?> values, Locale language) {
        return values.keySet()
                .stream()
                .filter(l -> !StringUtils.isEmpty(l.getLanguage()))
                .map(l -> (Entry<Locale, Locale>) new AbstractMap.SimpleEntry<>(l, Locale.forLanguageTag(l.getLanguage())))
                .filter(l -> l.getValue().equals(language)).findFirst();
    }

}
