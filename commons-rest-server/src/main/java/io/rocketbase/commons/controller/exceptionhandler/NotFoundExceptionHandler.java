package io.rocketbase.commons.controller.exceptionhandler;

import io.rocketbase.commons.dto.ErrorResponse;
import io.rocketbase.commons.exception.NotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
public class NotFoundExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(NOT_FOUND)
    @ResponseBody
    public ErrorResponse handleNotFoundException(NotFoundException e) {
        if (e.getErrorResponse() == null) {
            return new ErrorResponse(NOT_FOUND.value(), NOT_FOUND.getReasonPhrase());
        }
        return e.getErrorResponse();
    }
}
