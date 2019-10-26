package io.rocketbase.commons.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class QueryParamBuilder {

    /**
     * use default keys ("pageSize", "page", "sort") for pageable query
     *
     * @param uriBuilder instance that will get added queryParams
     * @param pageable   value to add to query
     * @return uriBuilder itself for fluent api
     */
    public static UriComponentsBuilder appendParams(UriComponentsBuilder uriBuilder, Pageable pageable) {
        return appendParams(uriBuilder, "pageSize", "page", "sort", pageable);
    }

    /**
     * custom queryKeys for pageable query
     *
     * @param uriBuilder  instance that will get added queryParams
     * @param pageSizeKey queryParam for getPageSize
     * @param pageKey     queryParam for getPageNumber
     * @param sortKey     queryParam for sort
     * @param value       value to add to query
     * @return uriBuilder itself for fluent api
     */
    public static UriComponentsBuilder appendParams(UriComponentsBuilder uriBuilder, String pageSizeKey, String pageKey, String sortKey, Pageable value) {
        if (uriBuilder == null || value == null) {
            return uriBuilder;
        }
        if (value.getPageNumber() >= 0) {
            uriBuilder.queryParam(Nulls.notEmpty(pageKey, "page"), value.getPageNumber());
        }
        if (value.getPageSize() >= 0) {
            uriBuilder.queryParam(Nulls.notEmpty(pageSizeKey, "pageSize"), value.getPageSize());
        }
        appendParams(uriBuilder, Nulls.notEmpty(sortKey, "sort"), value.getSort());
        return uriBuilder;
    }

    /**
     * in case you just want to add sort
     *
     * @param uriBuilder instance that will get added queryParams
     * @param key        key of sort parameter
     * @param value      value to add to query
     * @return uriBuilder itself for fluent api
     */
    public static UriComponentsBuilder appendParams(UriComponentsBuilder uriBuilder, String key, Sort value) {
        if (uriBuilder == null || key == null || value == null) {
            return uriBuilder;
        }
        value.iterator()
                .forEachRemaining(
                        o -> {
                            uriBuilder.queryParam(key, String.format("%s,%s", o.getProperty(), o.getDirection().name().toLowerCase()));
                        }
                );
        return uriBuilder;
    }

    /**
     * add number value to query
     *
     * @param uriBuilder instance that will get added queryParams
     * @param key        key of the given value
     * @param value      value to add to query
     * @return uriBuilder itself for fluent api
     */
    public static UriComponentsBuilder appendParams(UriComponentsBuilder uriBuilder, String key, Number value) {
        if (uriBuilder == null || key == null || value == null) {
            return uriBuilder;
        }
        uriBuilder.queryParam(key, String.valueOf(value));
        return uriBuilder;
    }


    /**
     * add Boolean value to query
     *
     * @param uriBuilder instance that will get added queryParams
     * @param key        key of the given value
     * @param value      value to add to query
     * @return uriBuilder itself for fluent api
     */
    public static UriComponentsBuilder appendParams(UriComponentsBuilder uriBuilder, String key, Boolean value) {
        if (uriBuilder == null || key == null || value == null) {
            return uriBuilder;
        }
        uriBuilder.queryParam(key, value ? "true" : "false");
        return uriBuilder;
    }


    /**
     * add LocalDate value to query
     *
     * @param uriBuilder instance that will get added queryParams
     * @param key        key of the given value
     * @param value      value to add to query
     * @return uriBuilder itself for fluent api
     */
    public static UriComponentsBuilder appendParams(UriComponentsBuilder uriBuilder, String key, LocalDate value) {
        if (uriBuilder == null || key == null || value == null) {
            return uriBuilder;
        }
        uriBuilder.queryParam(key, DateTimeFormatter.ISO_LOCAL_DATE.format(value));
        return uriBuilder;
    }


    /**
     * add LocalTime value to query
     *
     * @param uriBuilder instance that will get added queryParams
     * @param key        key of the given value
     * @param value      value to add to query
     * @return uriBuilder itself for fluent api
     */
    public static UriComponentsBuilder appendParams(UriComponentsBuilder uriBuilder, String key, LocalTime value) {
        if (uriBuilder == null || key == null || value == null) {
            return uriBuilder;
        }
        uriBuilder.queryParam(key, DateTimeFormatter.ISO_LOCAL_TIME.format(value));
        return uriBuilder;
    }


    /**
     * add LocalDateTime value  to query
     *
     * @param uriBuilder instance that will get added queryParams
     * @param key        key of the given value
     * @param value      value to add to query
     * @return uriBuilder itself for fluent api
     */
    public static UriComponentsBuilder appendParams(UriComponentsBuilder uriBuilder, String key, LocalDateTime value) {
        if (uriBuilder == null || key == null || value == null) {
            return uriBuilder;
        }
        uriBuilder.queryParam(key, DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(value));
        return uriBuilder;
    }


    /**
     * add Instant value to query
     *
     * @param uriBuilder instance that will get added queryParams
     * @param key        key of the given value
     * @param value      value to add to query
     * @return uriBuilder itself for fluent api
     */
    public static UriComponentsBuilder appendParams(UriComponentsBuilder uriBuilder, String key, Instant value) {
        if (uriBuilder == null || key == null || value == null) {
            return uriBuilder;
        }
        uriBuilder.queryParam(key, value.toString());
        return uriBuilder;
    }
}
