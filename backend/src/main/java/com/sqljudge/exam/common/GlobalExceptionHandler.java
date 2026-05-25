package com.sqljudge.exam.common;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> handleBusiness(BusinessException ex) {
        return ApiResponse.fail(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> handleValidation(Exception ex) {
        return ApiResponse.fail(40000, "参数校验失败");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> handleDenied() {
        return ApiResponse.fail(40300, "权限不足");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> handleUnknown(Exception ex) {
        return ApiResponse.fail(50000, ex.getMessage() == null ? "服务端错误" : ex.getMessage());
    }
}
