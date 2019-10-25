package io.rocketbase.commons.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import java.time.*;
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

    /**
     * parse page, size and sort from request
     *
     * @param params MultiValueMap that contains all query params of request
     * @return a filled {@link PageRequest}
     */
    default Pageable parsePageRequest(MultiValueMap<String, String> params) {
        return parsePageRequest(params, null);
    }

    /**
     * parse page, size and sort from request
     *
     * @param params      MultiValueMap that contains all query params of request
     * @param defaultSort sort that should get used in case of not filled parameter
     * @return a filled {@link PageRequest}
     */
    default Pageable parsePageRequest(MultiValueMap<String, String> params, Sort defaultSort) {
        Integer pageSize = parseInteger(params, "pageSize", null);
        if (pageSize == null) {
            pageSize = parseInteger(params, "size", getDefaultPageSize());
        }
        pageSize = Math.min(pageSize, getMaxPageSize());
        Integer page = parseInteger(params, "page", 0);

        return PageRequest.of(Math.max(page, 0), Math.max(pageSize, DEFAULT_MIN_PAGE_SIZE), parseSort(params, "sort", defaultSort));
    }


    /**
     * parse sort and build correct {@link Sort} or {@link org.springframework.data.domain.Pageable}
     *
     * @param params MultiValueMap that contains all query params of request
     * @param key    name of sort parameter in uri
     * @return an unsorted Sort in case of empty param or filled one
     */
    default Sort parseSort(MultiValueMap<String, String> params, String key) {
        return parseSort(params, key, null);
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
        Sort sort = defaultSort != null ? defaultSort : Sort.unsorted();

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

    default Instant parseInstant(MultiValueMap<String, String> params, String key, Instant defaultValue) {
        if (params != null) {
            String value = params.getFirst(key);
            if (value != null) {
                try {
                    Instant instant = Instant.parse(value);
                    return instant;
                } catch (DateTimeParseException ex) {
                    if (value.matches("[0-9]+")) {
                        long longValue = Long.parseLong(value);
                        int currentYear = LocalDate.now().getYear();

                        Instant instant = Instant.ofEpochSecond(longValue);
                        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
                        if (zonedDateTime.getYear() < currentYear + 100 && zonedDateTime.getYear() > 2001) {
                            return instant;
                        }
                        return defaultValue;
                    }
                }
            }
        }
        return defaultValue;
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
        String result = request.getScheme() + "://" + request.getServerName();
        int serverPort = request.getServerPort();
        if (serverPort != 80 && serverPort != 443) {
            result += ":" + serverPort;
        }
        result += request.getContextPath();
        if (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

}
