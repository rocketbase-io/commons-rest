package io.rocketbase.commons.controller.exceptionhandler;

import io.rocketbase.commons.dto.ErrorResponse;
import io.rocketbase.commons.exception.ObfuscatedDecodeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
public class ObfuscatedDecodeExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleNotFoundException(ObfuscatedDecodeException e) {
        return problem(NOT_FOUND, new ErrorResponse(NOT_FOUND.value(), "invalid id"));
    }
}
