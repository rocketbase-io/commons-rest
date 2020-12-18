package io.rocketbase.commons.exception;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ErrorCodes {
    FORM_ERROR("form_error", 1000);

    @JsonValue
    private final String value;

    @Getter
    private final int status;

}
