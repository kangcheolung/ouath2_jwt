package com.cotato.backend.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통 에러
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.", "COMMON-001"),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "요청 파라미터가 잘못되었습니다.", "COMMON-002"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "찾을 수 없습니다.", "COMMON-003"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부에서 에러가 발생하였습니다.", "COMMON-004"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.", "COMMON-005"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.", "COMMON-006"),

    // 유저 관련 에러
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다.", "USER-001"),

    // JWT 관련 에러
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.", "JWT-001"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다.", "JWT-002"),
    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "지원하지 않는 토큰입니다.", "JWT-003"),

    // OAuth 관련 에러
    OAUTH_PROVIDER_ERROR(HttpStatus.BAD_GATEWAY, "OAuth 제공자와의 통신에 실패했습니다.", "OAUTH-001"),
    ;

    private final HttpStatus httpStatus;
    private final String message;
    private final String code;
}