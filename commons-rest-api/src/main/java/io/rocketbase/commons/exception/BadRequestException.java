package io.rocketbase.commons.exception;

import io.rocketbase.commons.dto.ErrorResponse;
import lombok.Getter;

public class BadRequestException extends RuntimeException {

    @Getter
    private final ErrorResponse errorResponse;

    public BadRequestException() {
        super();
        this.errorResponse = null;
    }

    public BadRequestException(ErrorResponse errorResponse) {
        super(errorResponse.getMessage());
        this.errorResponse = errorResponse;
    }

    public BadRequestException(String message) {
        super(message);
        this.errorResponse = ErrorResponse.builder().message(message).build();
    }

}
