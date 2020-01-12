package io.rocketbase.commons.exception;

import io.rocketbase.commons.dto.ErrorResponse;
import lombok.Getter;

public class InsufficientPrivilegestException extends RuntimeException {

    @Getter
    private final ErrorResponse errorResponse;

    public InsufficientPrivilegestException() {
        super();
        this.errorResponse = null;
    }

    public InsufficientPrivilegestException(ErrorResponse errorResponse) {
        super(errorResponse.getMessage());
        this.errorResponse = errorResponse;
    }

    public InsufficientPrivilegestException(String message) {
        super(message);
        this.errorResponse = ErrorResponse.builder().message(message).build();
    }

}
