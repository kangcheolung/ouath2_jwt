package com.cotato.backend.domain.oauth;

import com.cotato.backend.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OAuth2Profile {
    private String name;
    private String email;
    private String provider;

    // OAuth2Profile → User 엔티티로 변환
    public User toUser() {
        return User.builder()
                .name(name)
                .email(email)
                .provider(provider)
                .build();
    }
}