package com.cotato.backend.oauth.service;

import com.cotato.backend.common.exception.AppException;
import com.cotato.backend.common.exception.ErrorCode;
import com.cotato.backend.oauth.dto.response.KakaoTokenResponse;
import com.cotato.backend.oauth.dto.response.KakaoUserInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class KakaoOAuthService {

    private final RestClient restClient = RestClient.create();

    @Value("${KAKAO_CLIENT_ID}")
    private String clientId;

    @Value("${KAKAO_CLIENT_SECRET:}")
    private String clientSecret;

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    public KakaoTokenResponse getAccessToken(String code, String redirectUri) {
        log.info("카카오 액세스 토큰 요청 시작 - code: {}, redirectUri: {}", code, redirectUri);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        if (clientSecret != null && !clientSecret.isEmpty()) {
            params.add("client_secret", clientSecret);
        }

        try {
            KakaoTokenResponse response = restClient.post()
                .uri(KAKAO_TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(params)
                .retrieve()
                .body(KakaoTokenResponse.class);

            log.info("카카오 액세스 토큰 발급 성공");
            return response;
        } catch (Exception e) {
            log.error("카카오 액세스 토큰 요청 실패", e);
            throw new AppException(ErrorCode.OAUTH_PROVIDER_ERROR);
        }
    }

    public KakaoUserInfoResponse getUserInfo(String accessToken) {
        log.info("카카오 사용자 정보 조회 시작");

        try {
            KakaoUserInfoResponse response = restClient.get()
                .uri(KAKAO_USER_INFO_URL)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(KakaoUserInfoResponse.class);

            log.info("카카오 사용자 정보 조회 성공");
            return response;
        } catch (Exception e) {
            log.error("카카오 사용자 정보 조회 실패", e);
            throw new AppException(ErrorCode.OAUTH_PROVIDER_ERROR);
        }
    }
}
