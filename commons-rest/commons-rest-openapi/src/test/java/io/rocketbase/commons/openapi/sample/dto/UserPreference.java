package io.rocketbase.commons.openapi.sample.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Schema(enumAsRef = true)
public enum UserPreference {

    FAVORED("favored"),
    DISFAVORED("disfavored");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static UserPreference lookup(String value) {
        if (value != null) {
            for (UserPreference v : values()) {
                if (v.name().equalsIgnoreCase(value) || v.value.equalsIgnoreCase(value)) {
                    return v;
                }
            }
        }
        return null;
    }
}
