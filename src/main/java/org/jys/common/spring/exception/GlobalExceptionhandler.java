package org.jys.common.spring.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.net.BindException;

/**
 * @author YueSong Jiang
 * @date 2019/3/16
 * @description <p> </p>
 */
@ControllerAdvice
public class GlobalExceptionhandler {

    @ExceptionHandler(value = BusinessException.class)
    public ErrorResponse handleBussinessException(BusinessException e, HttpServletResponse response) {
        response.setStatus(e.getResponseStatus());
        return new ErrorResponse(e.getErrorCode(), e.getErrorMessage());
    }

    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnknownException(Exception e) {
        return new ErrorResponse(500, e.getMessage());
    }

    @ExceptionHandler({MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class,
            ValidationException.class})
    public ErrorResponse handlebadRequestException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return new ErrorResponse(400, e.getMessage());
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentTypeMissmatchexception(MethodArgumentTypeMismatchException e) {
        return new ErrorResponse(400,
                String.format("input value type for parameter [%s] doesn't match! required value type is [%s]",
                        e.getName(), e.getRequiredType()));
    }
}
