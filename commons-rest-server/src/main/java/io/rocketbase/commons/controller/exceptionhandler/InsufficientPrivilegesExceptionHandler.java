package io.rocketbase.commons.controller.exceptionhandler;

import io.rocketbase.commons.dto.ErrorResponse;
import io.rocketbase.commons.exception.InsufficientPrivilegesException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@ControllerAdvice
public class InsufficientPrivilegesExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(FORBIDDEN)
    @ResponseBody
    public ErrorResponse handleBadRequestException(InsufficientPrivilegesException e) {
        if (e.getErrorResponse() == null) {
            return new ErrorResponse(FORBIDDEN.value(), FORBIDDEN.getReasonPhrase());
        }
        return e.getErrorResponse();
    }
}
