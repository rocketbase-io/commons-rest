package io.rocketbase.commons.controller;

import io.rocketbase.commons.util.QueryParamParser;
import io.rocketbase.commons.util.UrlParts;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * a until interface that provides convenient functions for MVC controller
 */
public interface BaseController {

    int DEFAULT_PAGE_SIZE = 25;
    int DEFAULT_MAX_PAGE_SIZE = 200;

    /**
     * parse page, size and sort from request
     *
     * @param params MultiValueMap that contains all query params of request
     * @return a filled {@link PageRequest}
     */
    default Pageable parsePageRequest(MultiValueMap<String, String> params) {
        return QueryParamParser.parsePageRequest(params);
    }

    /**
     * parse page, size and sort from request
     *
     * @param params      MultiValueMap that contains all query params of request
     * @param defaultSort sort that should get used in case of not filled parameter
     * @return a filled {@link PageRequest}
     */
    default Pageable parsePageRequest(MultiValueMap<String, String> params, Sort defaultSort) {
        return QueryParamParser.parsePageRequest(params, defaultSort, getDefaultPageSize(), getMaxPageSize());
    }


    /**
     * parse sort and build correct {@link Sort} or {@link org.springframework.data.domain.Pageable}
     *
     * @param params MultiValueMap that contains all query params of request
     * @param key    name of sort parameter in uri
     * @return an unsorted Sort in case of empty param or filled one
     */
    default Sort parseSort(MultiValueMap<String, String> params, String key) {
        return QueryParamParser.parseSort(params,key);
    }

    /**
     * parse sort and build correct {@link Sort} or {@link org.springframework.data.domain.Pageable}
     *
     * @param params      MultiValueMap that contains all query params of request
     * @param key         name of sort parameter in uri
     * @param defaultSort sort that should get used in case of not filled parameter
     * @return defaultSort in case of empty param or filled one
     */
    default Sort parseSort(MultiValueMap<String, String> params, String key, Sort defaultSort) {
        return QueryParamParser.parseSort(params, key, defaultSort);
    }

    default Integer parseInteger(MultiValueMap<String, String> params, String key, Integer defaultValue) {
        return QueryParamParser.parseInteger(params, key, defaultValue);
    }

    default Long parseLong(MultiValueMap<String, String> params, String key, Long defaultValue) {
        return QueryParamParser.parseLong(params, key, defaultValue);
    }

    default Boolean parseBoolean(MultiValueMap<String, String> params, String key, Boolean defaultValue) {
        return QueryParamParser.parseBoolean(params, key, defaultValue);
    }

    default LocalDate parseLocalDate(MultiValueMap<String, String> params, String key, LocalDate defaultValue) {
        return QueryParamParser.parseLocalDate(params, key, defaultValue);
    }

    default LocalTime parseLocalTime(MultiValueMap<String, String> params, String key, LocalTime defaultValue) {
        return QueryParamParser.parseLocalTime(params, key, defaultValue);
    }

    default LocalDateTime parseLocalDateTime(MultiValueMap<String, String> params, String key, LocalDateTime defaultValue) {
        return QueryParamParser.parseLocalDateTime(params, key, defaultValue);
    }

    default Instant parseInstant(MultiValueMap<String, String> params, String key, Instant defaultValue) {
        return QueryParamParser.parseInstant(params, key, defaultValue);
    }


    default int getDefaultPageSize() {
        return DEFAULT_PAGE_SIZE;
    }

    default int getMaxPageSize() {
        return DEFAULT_MAX_PAGE_SIZE;
    }

    /**
     * get baseUrl without / at the end
     *
     * @param request current HttpServletRequest
     * @return baseUrl without /
     */
    default String getBaseUrl(HttpServletRequest request) {
        return UrlParts.getBaseUrl(request);
    }

}
