package com.cotato.backend.domain.oauth;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public enum OAuth2Extractor {
    // 각 제공자별로 추출 함수를 매핑
    GOOGLE(OAuth2Provider.GOOGLE, OAuth2Extractor::extractGoogleProfile),
    NAVER(OAuth2Provider.NAVER, OAuth2Extractor::extractNaverProfile),
    KAKAO(OAuth2Provider.KAKAO, OAuth2Extractor::extractKakaoProfile);

    private final OAuth2Provider provider;
    private final Function<Map<String, Object>, OAuth2Profile> extractor;

    // 제공자에 맞는 추출 로직 실행
    public static OAuth2Profile extract(OAuth2Provider provider, Map<String, Object> attributes) {
        return Arrays.stream(values())
                .filter(extractor -> extractor.provider == provider)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid provider: " + provider))
                .extractor.apply(attributes);
    }

    private static OAuth2Profile extractGoogleProfile(Map<String, Object> attributes) {
        return OAuth2Profile.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .build();
    }

    private static OAuth2Profile extractNaverProfile(Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        if (response == null) {
            throw new IllegalArgumentException("네이버 프로필 정보를 가져올 수 없습니다.");
        }
        return OAuth2Profile.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .build();
    }

    private static OAuth2Profile extractKakaoProfile(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount == null) {
            throw new IllegalArgumentException("카카오 계정 정보를 가져올 수 없습니다.");
        }
        Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");
        if (kakaoProfile == null) {
            throw new IllegalArgumentException("카카오 프로필 정보를 가져올 수 없습니다.");
        }
        return OAuth2Profile.builder()
                .name((String) kakaoProfile.get("nickname"))
                .email((String) kakaoAccount.get("email"))
                .build();
    }
}