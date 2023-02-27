package io.rocketbase.commons.openapi.model;

import org.springframework.util.StringUtils;

public enum ReactQueryVersion {
    v3,
    v4;

    public static ReactQueryVersion parse(String version) {
        if (StringUtils.hasText(version) && (version.equalsIgnoreCase("v4") || version.equals("4"))) {
            return v4;
        }
        return v3;
    }
}
