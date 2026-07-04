package io.rocketbase.commons.controller.exceptionhandler;

import io.rocketbase.commons.dto.ErrorResponse;
import io.rocketbase.commons.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * exception will get later replaced by javax.ws.rs.BadRequestException so that this handler is not needed anymore
 */
@Deprecated
@ControllerAdvice
public class BadRequestExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException e) {
        return problem(BAD_REQUEST, e.getErrorResponse());
    }
}
