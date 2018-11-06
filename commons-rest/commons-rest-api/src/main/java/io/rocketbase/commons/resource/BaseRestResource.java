package io.rocketbase.commons.resource;

import io.rocketbase.commons.request.PageableRequest;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

public interface BaseRestResource {

    default UriComponentsBuilder appendParams(UriComponentsBuilder uriBuilder, PageableRequest request) {
        if (uriBuilder != null && request != null) {
            if (request.getPage() != null && request.getPage().intValue() >= 0) {
                uriBuilder.queryParam("page", request.getPage());
            }
            if (request.getPageSize() != null && request.getPageSize().intValue() >= 0) {
                uriBuilder.queryParam("pageSize", request.getPageSize());
            }
            if (request.getSort() != null) {
                request.getSort()
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
     * instantiate an UriComponentsBuilder from given url and ensures ends with slash
     *
     * @param baseApiUrl base url
     * @return UriComponentsBuilder
     */
    default UriComponentsBuilder createUriComponentsBuilder(String baseApiUrl) {
        return UriComponentsBuilder.fromUriString(ensureEndsWithSlash(baseApiUrl));
    }
}
