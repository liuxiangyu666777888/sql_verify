package com.sqljudge.exam.common;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(40000, message);
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException(40100, message);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(40300, message);
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(40400, message);
    }
}
