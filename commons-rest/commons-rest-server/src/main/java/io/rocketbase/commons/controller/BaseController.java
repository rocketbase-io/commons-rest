package io.rocketbase.commons.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

/**
 * a until interface that provides convenient functions for MVC controller
 */
public interface BaseController {

    int DEFAULT_PAGE_SIZE = 25;
    int DEFAULT_MAX_PAGE_SIZE = 200;

    List<DateTimeFormatter> DEFAULT_DATE_FORMATTERS = Arrays.asList(DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("d/MM/yyyy"),
            DateTimeFormatter.ofPattern("d.MM.yyyy"));

    default PageRequest parsePageRequest(MultiValueMap<String, String> params) {
        Integer pageSize = parseInteger(params, "pageSize", getPageSize());
        pageSize = Math.min(pageSize, getMaxPageSize());
        return new PageRequest(parseInteger(params, "page", 0), pageSize);
    }

    default Integer parseInteger(MultiValueMap<String, String> params, String key, Integer defaultValue) {
        Long value = parseLong(params, key, null);
        return value != null ? value.intValue() : defaultValue;
    }

    default Long parseLong(MultiValueMap<String, String> params, String key, Long defaultValue) {
        if (params != null) {
            String value = params.getFirst(key);
            if (value != null && value.matches("[0-9]+")) {
                try {
                    return Long.parseLong(value);
                } catch (NumberFormatException f) {
                }
            }
        }
        return defaultValue;
    }

    default Boolean parseBoolean(MultiValueMap<String, String> params, String key, Boolean defaultValue) {
        if (params != null) {
            String value = params.getFirst(key);
            if (value != null && value.matches("[true|1|yes|on]")) {
                return true;
            }
        }
        return defaultValue;
    }

    default LocalDate parseLocalDate(MultiValueMap<String, String> params, String key, LocalDate defaultValue) {
        if (params != null) {
            String value = params.getFirst(key);
            if (value != null) {
                for (DateTimeFormatter formatter : DEFAULT_DATE_FORMATTERS) {
                    try {
                        LocalDate localDate = LocalDate.parse(value, formatter);
                        return localDate;
                    } catch (DateTimeParseException ex) {
                    }
                }
                return defaultValue;
            }
        }
        return defaultValue;
    }

    default LocalTime parseLocalTime(MultiValueMap<String, String> params, String key, LocalTime defaultValue) {
        if (params != null) {
            String value = params.getFirst(key);
            if (value != null) {
                try {
                    LocalTime localTime = LocalTime.parse(value, DateTimeFormatter.ISO_LOCAL_TIME);
                    return localTime;
                } catch (DateTimeParseException ex) {
                    return defaultValue;
                }
            }
        }
        return defaultValue;
    }

    default LocalDateTime parseLocalDateTime(MultiValueMap<String, String> params, String key, LocalDateTime defaultValue) {
        if (params != null) {
            String value = params.getFirst(key);
            if (value != null) {
                try {
                    LocalDateTime localDateTime = LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    return localDateTime;
                } catch (DateTimeParseException ex) {
                    return defaultValue;
                }
            }
        }
        return defaultValue;
    }

    default int getPageSize() {
        return DEFAULT_PAGE_SIZE;
    }

    default int getMaxPageSize() {
        return DEFAULT_MAX_PAGE_SIZE;
    }

}
