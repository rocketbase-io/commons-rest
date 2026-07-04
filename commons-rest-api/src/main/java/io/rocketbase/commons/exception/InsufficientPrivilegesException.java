package io.rocketbase.commons.exception;

import io.rocketbase.commons.dto.ErrorResponse;
import lombok.Getter;

public class InsufficientPrivilegesException extends RuntimeException {

    @Getter
    private final ErrorResponse errorResponse;

    public InsufficientPrivilegesException() {
        super();
        this.errorResponse = null;
    }

    public InsufficientPrivilegesException(ErrorResponse errorResponse) {
        super(errorResponse.getDetail());
        this.errorResponse = errorResponse;
    }

    public InsufficientPrivilegesException(String message) {
        super(message);
        this.errorResponse = ErrorResponse.builder().detail(message).build();
    }

}
