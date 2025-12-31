package com.cotato.backend.domain.oauth;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class OAuth2UserAttribute {
    private final Map<String, Object> attributes;

    public OAuth2UserAttribute(Map<String, Object> oAuth2Attributes, String userNameAttributeName, OAuth2Profile userProfile) {
        // 원본 OAuth2 응답 + 파싱된 프로필 정보를 합침
        this.attributes = new LinkedHashMap<>(oAuth2Attributes);
        attributes.put(userNameAttributeName, oAuth2Attributes.get(userNameAttributeName));
        attributes.put("provider", userProfile.getProvider());
        attributes.put("name", userProfile.getName());
        attributes.put("email", userProfile.getEmail());
    }
}