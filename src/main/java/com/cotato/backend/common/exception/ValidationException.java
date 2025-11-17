package com.cotato.backend.common.exception;

// 400 Bad Request, 409 Conflict
public class ValidationException extends AppException {
    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
    }
}