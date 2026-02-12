package com.cotato.backend.oauth.service;

import com.cotato.backend.common.exception.AppException;
import com.cotato.backend.common.exception.ErrorCode;
import com.cotato.backend.oauth.dto.response.KakaoTokenResponse;
import com.cotato.backend.oauth.dto.response.KakaoUserInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${KAKAO_CLIENT_ID}")
    private String clientId;

    @Value("${KAKAO_CLIENT_SECRET:}")
    private String clientSecret;

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    /**
     * 인가코드로 카카오 액세스 토큰 요청
     */
    public KakaoTokenResponse getAccessToken(String code, String redirectUri) {
        log.info("카카오 액세스 토큰 요청 시작 - code: {}, redirectUri: {}", code, redirectUri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        if (clientSecret != null && !clientSecret.isEmpty()) {
            params.add("client_secret", clientSecret);
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<KakaoTokenResponse> response = restTemplate.exchange(
                KAKAO_TOKEN_URL,
                HttpMethod.POST,
                request,
                KakaoTokenResponse.class
            );

            log.info("카카오 액세스 토큰 발급 성공");
            return response.getBody();

        } catch (Exception e) {
            log.error("카카오 액세스 토큰 요청 실패", e);
            throw new AppException(ErrorCode.OAUTH_PROVIDER_ERROR);
        }
    }

    /**
     * 카카오 액세스 토큰으로 사용자 정보 조회
     */
    public KakaoUserInfoResponse getUserInfo(String accessToken) {
        log.info("카카오 사용자 정보 조회 시작");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<KakaoUserInfoResponse> response = restTemplate.exchange(
                KAKAO_USER_INFO_URL,
                HttpMethod.GET,
                request,
                KakaoUserInfoResponse.class
            );

            log.info("카카오 사용자 정보 조회 성공");
            return response.getBody();

        } catch (Exception e) {
            log.error("카카오 사용자 정보 조회 실패", e);
            throw new AppException(ErrorCode.OAUTH_PROVIDER_ERROR);
        }
    }
}
