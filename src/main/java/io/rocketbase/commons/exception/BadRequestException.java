package io.rocketbase.commons.exception;

import io.rocketbase.commons.dto.ErrorResponse;
import lombok.Getter;

public class BadRequestException extends RuntimeException {

    @Getter
    private final ErrorResponse errorResponse;

    public BadRequestException(ErrorResponse errorResponse) {
        super(errorResponse.getErrorMessage());
        this.errorResponse = errorResponse;
    }
}
