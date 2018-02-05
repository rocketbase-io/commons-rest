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
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class BeanValidationExceptionHandler extends BaseExceptionHandler {


    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleBeanValidationException(HttpServletRequest request, MethodArgumentNotValidException e) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(ErrorCodes.FORM_ERROR.getStatus());
        BindingResult bindingResult = e.getBindingResult();
        ObjectError globalError = bindingResult.getGlobalError();
        if (globalError != null) {
            response.setMessage(translate(request, globalError.getCode(), globalError.getDefaultMessage()));
        }

        Map<String, String> fieldMap = new HashMap<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            String field = fieldError.getField();
            String code = fieldError.getCode();
            String defaultMessage = fieldError.getDefaultMessage();
            fieldMap.put(field, translate(request, "error.form." + code, defaultMessage));
        }
        if (fieldMap.size() > 0) {
            response.setFields(fieldMap);
        }

        return response;
    }
}
