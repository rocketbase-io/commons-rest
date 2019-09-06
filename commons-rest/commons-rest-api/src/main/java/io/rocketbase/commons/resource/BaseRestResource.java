package io.rocketbase.commons.resource;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

public interface BaseRestResource {

    default UriComponentsBuilder appendParams(UriComponentsBuilder uriBuilder, Pageable pageable) {
        if (uriBuilder != null && pageable != null) {
            if (pageable.getPageNumber()  >= 0) {
                uriBuilder.queryParam("page", pageable.getPageNumber());
            }
            if (pageable.getPageSize() >= 0) {
                uriBuilder.queryParam("pageSize", pageable.getPageSize());
            }
            if (pageable.getSort() != null) {
                pageable.getSort()
                        .iterator()
                        .forEachRemaining(
                                o -> {
                                    uriBuilder.queryParam("sort", String.format("%s,%s", o.getProperty(), o.getDirection().name().toLowerCase()));
                                }
                        );
            }
        }
        return uriBuilder;
    }

    /**
     * create HttpHeaders with ACCEPT_LANGUAGE key of given {@link LocaleContextHolder}
     *
     * @return Headers with language key
     */
    default HttpHeaders createHeaderWithLanguage() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT_LANGUAGE,
                LocaleContextHolder.getLocale()
                        .getLanguage());
        return headers;
    }

    /**
     * checks if given uri ends with slash or adds it if missing
     *
     * @param uri given url
     * @return url with / at the end
     */
    default String ensureEndsWithSlash(String uri) {
        if (uri == null) {
            return "/";
        }
        if (!uri.endsWith("/")) {
            return String.format("%s/", uri);
        }
        return uri;
    }

    /**
     * checks if given uri starts with slash or adds it if missing
     *
     * @param uri given url
     * @return url with / at the beginning
     */
    default String ensureStartsWithSlash(String uri) {
        if (uri == null) {
            return "/";
        }
        if (!uri.startsWith("/")) {
            return String.format("/%s", uri);
        }
        return uri;
    }

    /**
     * checks if given uri ends with slash or adds it if missing
     *
     * @param path given path of url
     * @return path with / at beginning + end
     */
    default String ensureStartsAndEndsWithSlash(String path) {
        String fixedStart = ensureStartsWithSlash(path);
        return ensureEndsWithSlash(fixedStart);
    }

    /**
     * instantiate an UriComponentsBuilder from given url and ensures ends with slash
     *
     * @param baseApiUrl base url
     * @return UriComponentsBuilder
     */
    default UriComponentsBuilder createUriComponentsBuilder(String baseApiUrl) {
        return UriComponentsBuilder.fromUriString(ensureEndsWithSlash(baseApiUrl));
    }
}
