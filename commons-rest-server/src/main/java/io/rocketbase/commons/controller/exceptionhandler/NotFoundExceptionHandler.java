package io.rocketbase.commons.controller.exceptionhandler;

import io.rocketbase.commons.dto.ErrorResponse;
import io.rocketbase.commons.exception.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
public class NotFoundExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException e) {
        return problem(NOT_FOUND, e.getErrorResponse());
    }
}
