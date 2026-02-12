package com.cotato.backend.oauth.service;

import com.cotato.backend.common.exception.AppException;
import com.cotato.backend.common.exception.ErrorCode;
import com.cotato.backend.oauth.dto.GoogleTokenResponse;
import com.cotato.backend.oauth.dto.GoogleUserInfoResponse;
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
public class GoogleOAuthService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${GOOGLE_CLIENT_ID}")
    private String clientId;

    @Value("${GOOGLE_CLIENT_SECRET}")
    private String clientSecret;

    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    /**
     * 인가코드로 구글 액세스 토큰 요청
     */
    public GoogleTokenResponse getAccessToken(String code, String redirectUri) {
        log.info("구글 액세스 토큰 요청 시작 - code: {}, redirectUri: {}", code, redirectUri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<GoogleTokenResponse> response = restTemplate.exchange(
                GOOGLE_TOKEN_URL,
                HttpMethod.POST,
                request,
                GoogleTokenResponse.class
            );

            log.info("구글 액세스 토큰 발급 성공");
            return response.getBody();

        } catch (Exception e) {
            log.error("구글 액세스 토큰 요청 실패", e);
            throw new AppException(ErrorCode.OAUTH_PROVIDER_ERROR);
        }
    }

    /**
     * 구글 액세스 토큰으로 사용자 정보 조회
     */
    public GoogleUserInfoResponse getUserInfo(String accessToken) {
        log.info("구글 사용자 정보 조회 시작");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<GoogleUserInfoResponse> response = restTemplate.exchange(
                GOOGLE_USER_INFO_URL,
                HttpMethod.GET,
                request,
                GoogleUserInfoResponse.class
            );

            log.info("구글 사용자 정보 조회 성공");
            return response.getBody();

        } catch (Exception e) {
            log.error("구글 사용자 정보 조회 실패", e);
            throw new AppException(ErrorCode.OAUTH_PROVIDER_ERROR);
        }
    }
}
