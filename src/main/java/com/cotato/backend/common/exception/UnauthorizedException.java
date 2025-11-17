package com.cotato.backend.common.exception;

//401 Unauthorized
public class UnauthorizedException extends AppException {
    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }
}