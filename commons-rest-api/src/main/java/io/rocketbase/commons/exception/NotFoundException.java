package io.rocketbase.commons.exception;

import io.rocketbase.commons.dto.ErrorResponse;
import lombok.Getter;

/**
 * will get later replaced by javax.ws.rs.NotFoundException
 */
@Deprecated
public class NotFoundException extends RuntimeException {

    @Getter
    private final ErrorResponse errorResponse;

    public NotFoundException() {
        super();
        this.errorResponse = null;
    }

    public NotFoundException(ErrorResponse errorResponse) {
        super(errorResponse.getMessage());
        this.errorResponse = errorResponse;
    }

    public NotFoundException(String message) {
        super(message);
        this.errorResponse = ErrorResponse.builder().message(message).build();
    }
}
