package io.rocketbase.commons.exception;

import io.rocketbase.commons.dto.ErrorResponse;
import lombok.Getter;

public class NotFoundException extends RuntimeException {

    @Getter
    private final ErrorResponse errorResponse;

    public NotFoundException() {
        super();
        this.errorResponse = null;
    }

    public NotFoundException(ErrorResponse errorResponse) {
        super(errorResponse.getDetail());
        this.errorResponse = errorResponse;
    }

    public NotFoundException(String message) {
        super(message);
        this.errorResponse = ErrorResponse.builder().detail(message).build();
    }
}
