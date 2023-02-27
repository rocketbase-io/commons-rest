package io.rocketbase.commons.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;


public final class UrlParts {

    public static String concatPaths(Object... parts) {
        String result = "";
        if (parts != null) {
            for (Object p : parts) {
                String str = String.valueOf(p);
                if (p != null && StringUtils.hasText(str)) {
                    if (result.length() > 0 && !str.startsWith("/") && !result.endsWith("/")) {
                        result += "/";
                    }
                    result += str;
                }
            }
        }
        return result;
    }

    /**
     * checks if given uri ends with slash or adds it if missing
     *
     * @param uri given url
     * @return url with / at the end
     */
    public static String ensureEndsWithSlash(String uri) {
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
    public static String ensureStartsWithSlash(String uri) {
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
    public static String ensureStartsAndEndsWithSlash(String path) {
        String fixedStart = ensureStartsWithSlash(path);
        return ensureEndsWithSlash(fixedStart);
    }

    /**
     * check if given uri end with slash and removes it if existing
     *
     * @param path given path of url
     * @return path without / at end
     */
    public static String removeEndsWithSlash(String path) {
        String value = Nulls.notNull(path);
        if (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    /**
     * get baseUrl without / at the end
     *
     * @param request current HttpServletRequest
     * @return baseUrl without /
     */
    public static String getBaseUrl(HttpServletRequest request) {
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
