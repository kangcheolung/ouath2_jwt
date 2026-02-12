package com.cotato.backend.oauth.controller;

import com.cotato.backend.common.dto.DataResponse;
import com.cotato.backend.common.exception.AppException;
import com.cotato.backend.common.exception.ErrorCode;
import com.cotato.backend.common.jwt.JwtTokenProvider;
import com.cotato.backend.common.jwt.TokenBlacklistService;
import com.cotato.backend.domain.oauth.OAuth2Extractor;
import com.cotato.backend.domain.oauth.OAuth2Profile;
import com.cotato.backend.domain.oauth.OAuth2Provider;
import com.cotato.backend.domain.user.entity.User;
import com.cotato.backend.domain.user.repository.UserRepository;
import com.cotato.backend.oauth.dto.*;
import com.cotato.backend.oauth.service.GoogleOAuthService;
import com.cotato.backend.oauth.service.KakaoOAuthService;
import com.cotato.backend.oauth.service.NaverOAuthService;
import com.cotato.backend.user.dto.UserInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 API")
public class OAuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final TokenBlacklistService tokenBlacklistService;
    private final KakaoOAuthService kakaoOAuthService;
    private final NaverOAuthService naverOAuthService;
    private final GoogleOAuthService googleOAuthService;

    // 카카오 로그인 콜백
    @PostMapping("/kakao/callback")
    @Operation(summary = "카카오 로그인 콜백", description = "프론트엔드에서 받은 인가코드로 JWT 발급")
    public ResponseEntity<DataResponse<TokenResponse>> kakaoCallback(
        @RequestBody KakaoCallbackRequest request) {

        log.info("카카오 로그인 콜백 시작 - code: {}, redirectUri: {}", request.getCode(), request.getRedirectUri());

        // 1. 카카오에 액세스 토큰 요청
        KakaoTokenResponse kakaoToken = kakaoOAuthService.getAccessToken(
            request.getCode(),
            request.getRedirectUri()
        );

        // 2. 카카오 사용자 정보 조회
        KakaoUserInfoResponse kakaoUserInfo = kakaoOAuthService.getUserInfo(kakaoToken.getAccessToken());

        // 3. OAuth2Extractor로 프로필 추출 (id를 attributes에 포함)
        java.util.Map<String, Object> attributes = new java.util.HashMap<>(kakaoUserInfo.getKakaoAccount());
        attributes.put("id", kakaoUserInfo.getId());

        OAuth2Profile profile = OAuth2Extractor.extract(OAuth2Provider.KAKAO, attributes);
        profile.setProvider(OAuth2Provider.KAKAO.getRegistrationId());

        // 4. 사용자 조회 또는 생성
        User user = userRepository.findByEmailAndProvider(profile.getEmail(), profile.getProvider())
            .orElseGet(() -> {
                User newUser = profile.toUser();
                return userRepository.save(newUser);
            });

        log.info("사용자 로그인 성공 - userId: {}, email: {}", user.getId(), user.getEmail());

        // 5. JWT 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        TokenResponse tokenResponse = TokenResponse.of(accessToken, refreshToken);
        return ResponseEntity.ok(DataResponse.from(tokenResponse));
    }

    // 네이버 로그인 콜백
    @PostMapping("/naver/callback")
    @Operation(summary = "네이버 로그인 콜백", description = "프론트엔드에서 받은 인가코드로 JWT 발급")
    public ResponseEntity<DataResponse<TokenResponse>> naverCallback(
        @RequestBody NaverCallbackRequest request) {

        log.info("네이버 로그인 콜백 시작 - code: {}, state: {}, redirectUri: {}",
            request.getCode(), request.getState(), request.getRedirectUri());

        // 1. 네이버에 액세스 토큰 요청
        NaverTokenResponse naverToken = naverOAuthService.getAccessToken(
            request.getCode(),
            request.getState(),
            request.getRedirectUri()
        );

        // 2. 네이버 사용자 정보 조회
        NaverUserInfoResponse naverUserInfo = naverOAuthService.getUserInfo(naverToken.getAccessToken());

        // 3. OAuth2Extractor로 프로필 추출
        OAuth2Profile profile = OAuth2Extractor.extract(OAuth2Provider.NAVER, naverUserInfo.getResponse());
        profile.setProvider(OAuth2Provider.NAVER.getRegistrationId());

        // 4. 사용자 조회 또는 생성
        User user = userRepository.findByEmailAndProvider(profile.getEmail(), profile.getProvider())
            .orElseGet(() -> {
                User newUser = profile.toUser();
                return userRepository.save(newUser);
            });

        log.info("사용자 로그인 성공 - userId: {}, email: {}", user.getId(), user.getEmail());

        // 5. JWT 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        TokenResponse tokenResponse = TokenResponse.of(accessToken, refreshToken);
        return ResponseEntity.ok(DataResponse.from(tokenResponse));
    }

    // 구글 로그인 콜백
    @PostMapping("/google/callback")
    @Operation(summary = "구글 로그인 콜백", description = "프론트엔드에서 받은 인가코드로 JWT 발급")
    public ResponseEntity<DataResponse<TokenResponse>> googleCallback(
        @RequestBody GoogleCallbackRequest request) {

        log.info("구글 로그인 콜백 시작 - code: {}, redirectUri: {}", request.getCode(), request.getRedirectUri());

        // 1. 구글에 액세스 토큰 요청
        GoogleTokenResponse googleToken = googleOAuthService.getAccessToken(
            request.getCode(),
            request.getRedirectUri()
        );

        // 2. 구글 사용자 정보 조회
        GoogleUserInfoResponse googleUserInfo = googleOAuthService.getUserInfo(googleToken.getAccessToken());

        // 3. OAuth2Extractor로 프로필 추출
        java.util.Map<String, Object> attributes = new java.util.HashMap<>();
        attributes.put("name", googleUserInfo.getName());
        attributes.put("email", googleUserInfo.getEmail());

        OAuth2Profile profile = OAuth2Extractor.extract(OAuth2Provider.GOOGLE, attributes);
        profile.setProvider(OAuth2Provider.GOOGLE.getRegistrationId());

        // 4. 사용자 조회 또는 생성
        User user = userRepository.findByEmailAndProvider(profile.getEmail(), profile.getProvider())
            .orElseGet(() -> {
                User newUser = profile.toUser();
                return userRepository.save(newUser);
            });

        log.info("사용자 로그인 성공 - userId: {}, email: {}", user.getId(), user.getEmail());

        // 5. JWT 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        TokenResponse tokenResponse = TokenResponse.of(accessToken, refreshToken);
        return ResponseEntity.ok(DataResponse.from(tokenResponse));
    }

    // 인증된 현재 사용자 정보 조회
    @GetMapping("/me")
    @Operation(summary = "현재 사용자 정보 조회")
    @SecurityRequirement(name = "accessTokenAuth")
    public ResponseEntity<DataResponse<UserInfoResponse>> getCurrentUser(
        @Parameter(hidden = true) @AuthenticationPrincipal User user) {

        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        UserInfoResponse userInfo = UserInfoResponse.from(user);
        return ResponseEntity.ok(DataResponse.from(userInfo));
    }

    // Token 갱신
    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급받습니다.")
    public ResponseEntity<DataResponse<TokenResponse>> refresh(
        @RequestHeader("Authorization") String refreshToken) {

        String token = refreshToken.replace("Bearer ", "");

        // 블랙리스트 확인 (로그아웃된 토큰인지)
        if (tokenBlacklistService.isBlacklisted(token)) {
            log.warn("블랙리스트에 등록된 Refresh Token으로 갱신 시도");
            return ResponseEntity.status(401).build();
        }

        // 토큰 유효성 검증
        if (!jwtTokenProvider.validateToken(token)) {
            log.warn("유효하지 않은 Refresh Token으로 갱신 시도");
            return ResponseEntity.status(401).build();
        }

        // Refresh Token 타입인지 확인
        if (!jwtTokenProvider.isRefreshToken(token)) {
            log.warn("Refresh Token이 아닌 토큰으로 갱신 시도");
            return ResponseEntity.status(401).build();
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        TokenResponse tokenResponse = TokenResponse.of(newAccessToken, newRefreshToken);
        return ResponseEntity.ok(DataResponse.from(tokenResponse));
    }

    // Token 검증
    @GetMapping("/validate")
    @Operation(summary = "토큰 검증")
    @SecurityRequirement(name = "accessTokenAuth")
    public ResponseEntity<DataResponse<String>> validateToken(
        @RequestHeader("Authorization") String token) {

        String jwt = token.replace("Bearer ", "");
        boolean isValid = jwtTokenProvider.validateToken(jwt);

        if (isValid) {
            Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
            String email = jwtTokenProvider.getEmailFromToken(jwt);
            String message = String.format("유효한 토큰입니다. User ID: %d, Email: %s", userId, email);
            return ResponseEntity.ok(DataResponse.from(message));
        } else {
            return ResponseEntity.status(401)
                .body(DataResponse.from("유효하지 않은 토큰입니다."));
        }
    }

    // 로그아웃
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "Access Token과 Refresh Token을 블랙리스트에 등록하여 무효화합니다.")
    @SecurityRequirement(name = "accessTokenAuth")
    public ResponseEntity<DataResponse<String>> logout(
        @RequestHeader("Authorization") String accessToken,
        @RequestBody LogoutRequest logoutRequest) {

        String access = accessToken.replace("Bearer ", "");
        String refresh = logoutRequest.getRefreshToken();

        // Access Token 블랙리스트 등록
        if (jwtTokenProvider.validateToken(access)) {
            long accessExpiration = jwtTokenProvider.getRemainingExpiration(access);
            tokenBlacklistService.addToBlacklist(access, accessExpiration);
        }

        // Refresh Token 블랙리스트 등록
        if (refresh != null && jwtTokenProvider.validateToken(refresh)) {
            long refreshExpiration = jwtTokenProvider.getRemainingExpiration(refresh);
            tokenBlacklistService.addToBlacklist(refresh, refreshExpiration);
        }

        log.info("로그아웃 처리 완료");
        return ResponseEntity.ok(DataResponse.from("로그아웃 되었습니다."));
    }
}