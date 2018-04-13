package io.rocketbase.commons.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * a until interface that provides convenient functions for MVC controller
 */
public interface BaseController {

    int DEFAULT_PAGE_SIZE = 25;
    int DEFAULT_MIN_PAGE_SIZE = 1;
    int DEFAULT_MAX_PAGE_SIZE = 200;

    List<DateTimeFormatter> DEFAULT_DATE_FORMATTERS = Arrays.asList(DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("d/MM/yyyy"),
            DateTimeFormatter.ofPattern("d.MM.yyyy"));

    default PageRequest parsePageRequest(MultiValueMap<String, String> params) {
        Integer pageSize = parseInteger(params, "pageSize", getPageSize());
        pageSize = Math.min(pageSize, getMaxPageSize());
        Integer page = parseInteger(params, "page", 0);

        return PageRequest.of(Math.max(page, 0), Math.max(pageSize, DEFAULT_MIN_PAGE_SIZE), parseSort(params, "sort"));
    }

    default Sort parseSort(MultiValueMap<String, String> params, String key) {
        Sort sort = Sort.unsorted();
        if (params != null && params.containsKey(key)) {
            List<Sort.Order> orders = new ArrayList<>();
            for (String s : params.get(key)) {
                if (s.toLowerCase().matches("[a-z0-9]+\\,(asc|desc)")) {
                    String[] splitted = s.split("\\,");
                    orders.add(new Sort.Order(Sort.Direction.fromString(splitted[1]), splitted[0]));
                } else if (s.toLowerCase().matches("[a-z0-9]+")) {
                    orders.add(Sort.Order.by(s));
                }
            }
            if (orders.size() > 0) {
                sort = Sort.by(orders);
            }
        }
        return sort;
    }

    default Integer parseInteger(MultiValueMap<String, String> params, String key, Integer defaultValue) {
        Long value = parseLong(params, key, null);
        if (value != null) {
            return value.intValue();
        }
        return defaultValue;
    }

    default Long parseLong(MultiValueMap<String, String> params, String key, Long defaultValue) {
        if (params != null) {
            String value = params.getFirst(key);
            if (value != null && value.matches("-?[0-9]+")) {
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
            if (value != null) {
                return value.matches("(true|1|yes|on)");
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
