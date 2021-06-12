package io.rocketbase.commons.exception;

import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Schema(enumAsRef = true)
public enum ErrorCodes {
    FORM_ERROR("form-error", 1000);

    private final String value;

    @Getter
    private final int status;

    @JsonValue
    public String getValue() {
        return value;
    }
}
