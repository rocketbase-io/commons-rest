package io.rocketbase.commons.controller.exceptionhandler;

import io.rocketbase.commons.dto.ErrorResponse;
import io.rocketbase.commons.exception.ErrorCodes;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class BeanValidationExceptionHandler extends BaseExceptionHandler {


    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleBeanValidationException(HttpServletRequest request, MethodArgumentNotValidException e) {
        ErrorResponse.ErrorResponseBuilder builder = ErrorResponse.builder()
                .errorCode(ErrorCodes.FORM_ERROR.getErrorCode());
        BindingResult bindingResult = e.getBindingResult();
        ObjectError globalError = bindingResult.getGlobalError();
        if (globalError != null) {
            builder.errorMessage(translate(request, globalError.getCode(), globalError.getDefaultMessage()));
        }

        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            String field = fieldError.getField();
            String code = fieldError.getCode();
            String defaultMessage = fieldError.getDefaultMessage();
            builder.field(field, translate(request, "error.form." + code, defaultMessage));
        }

        return builder.build();
    }
}
