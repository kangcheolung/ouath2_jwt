package com.cotato.backend.domain.oauth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    USER("USER"); // 일반 사용자 권한, ADMIN 등의 다른 권한 추가 가능

    private final String value;
}