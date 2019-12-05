package io.rocketbase.commons.controller.exceptionhandler;

import io.rocketbase.commons.dto.ErrorResponse;
import io.rocketbase.commons.exception.ObfuscatedDecodeException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
public class ObfuscatedDecodeExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(NOT_FOUND)
    @ResponseBody
    public ErrorResponse handleNotFoundException(ObfuscatedDecodeException e) {
        return new ErrorResponse(NOT_FOUND.value(), "invalid id");
    }
}
