package com.cotato.backend.user.dto;

import com.cotato.backend.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {
    private Long userId;
    private String email;
    private String name;
    private String provider;

    public static UserInfoResponse from(User user) {
        return UserInfoResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .provider(user.getProvider())
                .build();
    }
}