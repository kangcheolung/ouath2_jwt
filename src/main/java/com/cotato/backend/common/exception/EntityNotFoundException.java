package com.cotato.backend.common.exception;

// 404 Not Found
public class EntityNotFoundException extends AppException {
    public EntityNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}