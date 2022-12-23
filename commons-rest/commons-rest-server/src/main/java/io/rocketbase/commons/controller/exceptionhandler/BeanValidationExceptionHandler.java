package io.rocketbase.commons.controller.exceptionhandler;

import io.rocketbase.commons.dto.ErrorResponse;
import io.rocketbase.commons.exception.ErrorCodes;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;


@ControllerAdvice
@Slf4j
public class BeanValidationExceptionHandler extends BaseExceptionHandler {


    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleBeanValidationException(HttpServletRequest request, MethodArgumentNotValidException e) {
        ErrorResponse result = new ErrorResponse(ErrorCodes.FORM_ERROR.getStatus(), null);

        BindingResult bindingResult = e.getBindingResult();
        ObjectError globalError = bindingResult.getGlobalError();
        if (globalError != null) {
            result.setMessage(translate(request, globalError.getCode(), globalError.getDefaultMessage()));
        } else {
            result.setMessage(translate(request, "error.form", "invalid form"));
        }

        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            String code = fieldError.getCode();
            String defaultMessage = fieldError.getDefaultMessage();
            result.addField(fieldError.getField(), translate(request, "error.form." + code, defaultMessage));
        }

        if (log.isDebugEnabled()) {
            log.debug("[{}] {} throws MethodArgumentNotValidException fieldErrors: {}", request.getMethod(), request.getContextPath(), result.getFields());
        }

        return result;
    }
}
