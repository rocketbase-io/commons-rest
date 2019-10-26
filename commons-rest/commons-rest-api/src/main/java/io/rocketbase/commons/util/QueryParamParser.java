package io.rocketbase.commons.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.MultiValueMap;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class QueryParamParser {

    public static List<DateTimeFormatter> DEFAULT_DATE_FORMATTERS = Arrays.asList(DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("d/MM/yyyy"),
            DateTimeFormatter.ofPattern("d.MM.yyyy"));

    /**
     * parse page, size and sort from request
     *
     * @param params MultiValueMap that contains all query params of request
     * @return a filled {@link PageRequest}
     */
    public static Pageable parsePageRequest(MultiValueMap<String, String> params) {
        return parsePageRequest(params, null, 25, 200);
    }

    /**
     * parse page, size and sort from request
     *
     * @param params          MultiValueMap that contains all query params of request
     * @param defaultSort     sort that should get used in case of not filled parameter
     * @param defaultPageSize in case the pageSize is not set - which value to take
     * @param maxPageSize     maximum size of pageSize to limit the response size
     * @return a filled {@link PageRequest}
     */
    public static Pageable parsePageRequest(MultiValueMap<String, String> params, Sort defaultSort, int defaultPageSize, int maxPageSize) {
        Integer pageSize = parseInteger(params, "pageSize", null);
        if (pageSize == null) {
            pageSize = parseInteger(params, "size", defaultPageSize);
        }
        pageSize = Math.min(pageSize, maxPageSize);
        Integer page = parseInteger(params, "page", 0);

        return PageRequest.of(Math.max(page, 0), Math.max(pageSize, 1), parseSort(params, "sort", defaultSort));
    }

    /**
     * parse page, size and sort from request
     *
     * @param params          MultiValueMap that contains all query params of request
     * @param pageSizeKey     queryParam of current size of page to query
     * @param pageKey         queryParam of current page to query
     * @param sortKey         queryParam of sort to query
     * @param defaultSort     sort that should get used in case of not filled parameter
     * @param defaultPageSize in case the pageSize is not set - which value to take
     * @param maxPageSize     maximum size of pageSize to limit the response size
     * @return a filled {@link PageRequest}
     */
    public static Pageable parsePageRequest(MultiValueMap<String, String> params, String pageSizeKey, String pageKey, String sortKey, Sort defaultSort, int defaultPageSize, int maxPageSize) {
        Integer pageSize = parseInteger(params, pageSizeKey, defaultPageSize);
        pageSize = Math.min(pageSize, maxPageSize);
        Integer page = parseInteger(params, pageKey, 0);

        return PageRequest.of(Math.max(page, 0), Math.max(pageSize, 1), parseSort(params, sortKey, defaultSort));
    }


    /**
     * parse sort and build correct {@link Sort} or {@link org.springframework.data.domain.Pageable}
     *
     * @param params MultiValueMap that contains all query params of request
     * @param key    name of sort parameter in uri
     * @return an unsorted Sort in case of empty param or filled one
     */
    public static Sort parseSort(MultiValueMap<String, String> params, String key) {
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
    public static Sort parseSort(MultiValueMap<String, String> params, String key, Sort defaultSort) {
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

    public static Integer parseInteger(MultiValueMap<String, String> params, String key, Integer defaultValue) {
        Long value = parseLong(params, key, null);
        if (value != null) {
            return value.intValue();
        }
        return defaultValue;
    }

    public static Long parseLong(MultiValueMap<String, String> params, String key, Long defaultValue) {
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

    public static Boolean parseBoolean(MultiValueMap<String, String> params, String key, Boolean defaultValue) {
        if (params != null) {
            String value = params.getFirst(key);
            if (value != null) {
                return value.matches("(true|1|yes|on)");
            }
        }
        return defaultValue;
    }

    public static LocalDate parseLocalDate(MultiValueMap<String, String> params, String key, LocalDate defaultValue) {
        if (params != null) {
            String value = params.getFirst(key);
            if (value != null) {
                for (DateTimeFormatter formatter : DEFAULT_DATE_FORMATTERS) {
                    try {
                        return LocalDate.parse(value, formatter);
                    } catch (DateTimeParseException ex) {
                    }
                }
            }
        }
        return defaultValue;
    }

    public static LocalTime parseLocalTime(MultiValueMap<String, String> params, String key, LocalTime defaultValue) {
        if (params != null) {
            String value = params.getFirst(key);
            if (value != null) {
                try {
                    return LocalTime.parse(value, DateTimeFormatter.ISO_LOCAL_TIME);
                } catch (DateTimeParseException ex) {
                }
            }
        }
        return defaultValue;
    }

    public static LocalDateTime parseLocalDateTime(MultiValueMap<String, String> params, String key, LocalDateTime defaultValue) {
        if (params != null) {
            String value = params.getFirst(key);
            if (value != null) {
                try {
                    return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (DateTimeParseException ex) {
                    try {
                        return Instant.parse(value).atZone(ZoneOffset.systemDefault().getRules().getOffset(LocalDateTime.now())).toLocalDateTime();
                    } catch (DateTimeException eex) {
                    }
                }
            }
        }
        return defaultValue;
    }

    public static Instant parseInstant(MultiValueMap<String, String> params, String key, Instant defaultValue) {
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
                    }
                }
            }
        }
        return defaultValue;
    }

}
