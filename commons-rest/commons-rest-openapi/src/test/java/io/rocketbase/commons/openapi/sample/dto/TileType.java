package io.rocketbase.commons.openapi.sample.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Schema(enumAsRef = true)
public enum TileType {
    SEARCH_CONFIG("search-config"),
    PINBOARD("pinboard"),
    BRIEFING("briefing"),
    SHOWROOM("showroom");

    @JsonValue
    private final String value;

    public String getValue() {
        return this.value;
    }

    public static TileType findByNameOrValue(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        for (TileType t : TileType.values()) {
            if (value.equalsIgnoreCase(t.name()) || value.equalsIgnoreCase(t.getValue())) {
                return t;
            }
        }
        return null;
    }
}
