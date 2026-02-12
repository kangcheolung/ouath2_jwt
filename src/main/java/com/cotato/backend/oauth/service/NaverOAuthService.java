package com.cotato.backend.oauth.service;

import com.cotato.backend.common.exception.AppException;
import com.cotato.backend.common.exception.ErrorCode;
import com.cotato.backend.oauth.dto.response.NaverTokenResponse;
import com.cotato.backend.oauth.dto.response.NaverUserInfoResponse;
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
public class NaverOAuthService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${NAVER_CLIENT_ID}")
    private String clientId;

    @Value("${NAVER_CLIENT_SECRET}")
    private String clientSecret;

    private static final String NAVER_TOKEN_URL = "https://nid.naver.com/oauth2.0/token";
    private static final String NAVER_USER_INFO_URL = "https://openapi.naver.com/v1/nid/me";

    /**
     * 인가코드로 네이버 액세스 토큰 요청
     */
    public NaverTokenResponse getAccessToken(String code, String state, String redirectUri) {
        log.info("네이버 액세스 토큰 요청 시작 - code: {}, state: {}, redirectUri: {}", code, state, redirectUri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("state", state);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<NaverTokenResponse> response = restTemplate.exchange(
                NAVER_TOKEN_URL,
                HttpMethod.POST,
                request,
                NaverTokenResponse.class
            );

            log.info("네이버 액세스 토큰 발급 성공");
            return response.getBody();

        } catch (Exception e) {
            log.error("네이버 액세스 토큰 요청 실패", e);
            throw new AppException(ErrorCode.OAUTH_PROVIDER_ERROR);
        }
    }

    /**
     * 네이버 액세스 토큰으로 사용자 정보 조회
     */
    public NaverUserInfoResponse getUserInfo(String accessToken) {
        log.info("네이버 사용자 정보 조회 시작");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<NaverUserInfoResponse> response = restTemplate.exchange(
                NAVER_USER_INFO_URL,
                HttpMethod.GET,
                request,
                NaverUserInfoResponse.class
            );

            log.info("네이버 사용자 정보 조회 성공");
            return response.getBody();

        } catch (Exception e) {
            log.error("네이버 사용자 정보 조회 실패", e);
            throw new AppException(ErrorCode.OAUTH_PROVIDER_ERROR);
        }
    }
}
