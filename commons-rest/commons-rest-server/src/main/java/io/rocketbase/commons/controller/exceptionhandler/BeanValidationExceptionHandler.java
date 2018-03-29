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
                .status(ErrorCodes.FORM_ERROR.getStatus());

        BindingResult bindingResult = e.getBindingResult();
        ObjectError globalError = bindingResult.getGlobalError();
        if (globalError != null) {
            builder.message(translate(request, globalError.getCode(), globalError.getDefaultMessage()));
        } else {
            builder.message(translate(request, "error.form", "invalid form"));
        }

        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            String code = fieldError.getCode();
            String defaultMessage = fieldError.getDefaultMessage();
            builder.field(fieldError.getField(), translate(request, "error.form." + code, defaultMessage));
        }

        return builder.build();
    }
}
