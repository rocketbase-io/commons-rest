package io.rocketbase.commons.controller.exceptionhandler;

import io.rocketbase.commons.dto.ErrorResponse;
import io.rocketbase.commons.exception.InsufficientPrivilegesException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@ControllerAdvice
public class InsufficientPrivilegesExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleInsufficientPrivilegesException(InsufficientPrivilegesException e) {
        return problem(FORBIDDEN, e.getErrorResponse());
    }
}
