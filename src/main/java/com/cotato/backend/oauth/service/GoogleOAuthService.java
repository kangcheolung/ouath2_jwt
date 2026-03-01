package com.cotato.backend.oauth.service;

import com.cotato.backend.common.exception.AppException;
import com.cotato.backend.common.exception.ErrorCode;
import com.cotato.backend.oauth.dto.response.GoogleTokenResponse;
import com.cotato.backend.oauth.dto.response.GoogleUserInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class GoogleOAuthService {

    private final RestClient restClient = RestClient.create();

    @Value("${GOOGLE_CLIENT_ID}")
    private String clientId;

    @Value("${GOOGLE_CLIENT_SECRET}")
    private String clientSecret;

    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    public GoogleTokenResponse getAccessToken(String code, String redirectUri) {
        log.info("구글 액세스 토큰 요청 시작 - code: {}, redirectUri: {}", code, redirectUri);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("redirect_uri", redirectUri);

        try {
            GoogleTokenResponse response = restClient.post()
                .uri(GOOGLE_TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(params)
                .retrieve()
                .body(GoogleTokenResponse.class);

            log.info("구글 액세스 토큰 발급 성공");
            return response;
        } catch (Exception e) {
            log.error("구글 액세스 토큰 요청 실패", e);
            throw new AppException(ErrorCode.OAUTH_PROVIDER_ERROR);
        }
    }

    public GoogleUserInfoResponse getUserInfo(String accessToken) {
        log.info("구글 사용자 정보 조회 시작");

        try {
            GoogleUserInfoResponse response = restClient.get()
                .uri(GOOGLE_USER_INFO_URL)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(GoogleUserInfoResponse.class);

            log.info("구글 사용자 정보 조회 성공");
            return response;
        } catch (Exception e) {
            log.error("구글 사용자 정보 조회 실패", e);
            throw new AppException(ErrorCode.OAUTH_PROVIDER_ERROR);
        }
    }
}
