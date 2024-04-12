package io.rocketbase.commons.openapi.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public enum ReactQueryVersion {
    v3(3, "react-query"),
    v4(4, "@tanstack/react-query"),
    v5(5, "@tanstack/react-query");

    @Getter
    protected final int number;
    @Getter
    protected final String packageName;

    public static ReactQueryVersion parse(String version) {
        if (StringUtils.hasText(version) && (version.equalsIgnoreCase("v5") || version.equals("5"))) {
            return v5;
        } else if (StringUtils.hasText(version) && (version.equalsIgnoreCase("v4") || version.equals("4"))) {
            return v4;
        }
        return v3;
    }

}
