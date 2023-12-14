package io.rocketbase.commons.controller.exceptionhandler;

import io.rocketbase.commons.dto.ErrorResponse;
import io.rocketbase.commons.exception.BadRequestException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * exception will get later replaced by javax.ws.rs.BadRequestException so that this handler is not needed anymore
 */
@Deprecated
@ControllerAdvice
public class BadRequestExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleBadRequestException(BadRequestException e) {
        if (e.getErrorResponse() == null) {
            return new ErrorResponse(BAD_REQUEST.value(), BAD_REQUEST.getReasonPhrase());
        }
        return e.getErrorResponse();
    }
}
