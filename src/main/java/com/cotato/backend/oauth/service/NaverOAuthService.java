package com.cotato.backend.oauth.service;

import com.cotato.backend.common.exception.AppException;
import com.cotato.backend.common.exception.ErrorCode;
import com.cotato.backend.oauth.dto.response.NaverTokenResponse;
import com.cotato.backend.oauth.dto.response.NaverUserInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class NaverOAuthService {

    private final RestClient restClient = RestClient.create();

    @Value("${NAVER_CLIENT_ID}")
    private String clientId;

    @Value("${NAVER_CLIENT_SECRET}")
    private String clientSecret;

    private static final String NAVER_TOKEN_URL = "https://nid.naver.com/oauth2.0/token";
    private static final String NAVER_USER_INFO_URL = "https://openapi.naver.com/v1/nid/me";

    public NaverTokenResponse getAccessToken(String code, String state, String redirectUri) {
        log.info("네이버 액세스 토큰 요청 시작 - code: {}, state: {}, redirectUri: {}", code, state, redirectUri);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("state", state);

        try {
            NaverTokenResponse response = restClient.post()
                .uri(NAVER_TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(params)
                .retrieve()
                .body(NaverTokenResponse.class);

            log.info("네이버 액세스 토큰 발급 성공");
            return response;
        } catch (Exception e) {
            log.error("네이버 액세스 토큰 요청 실패", e);
            throw new AppException(ErrorCode.OAUTH_PROVIDER_ERROR);
        }
    }

    public NaverUserInfoResponse getUserInfo(String accessToken) {
        log.info("네이버 사용자 정보 조회 시작");

        try {
            NaverUserInfoResponse response = restClient.get()
                .uri(NAVER_USER_INFO_URL)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(NaverUserInfoResponse.class);

            log.info("네이버 사용자 정보 조회 성공");
            return response;
        } catch (Exception e) {
            log.error("네이버 사용자 정보 조회 실패", e);
            throw new AppException(ErrorCode.OAUTH_PROVIDER_ERROR);
        }
    }
}
