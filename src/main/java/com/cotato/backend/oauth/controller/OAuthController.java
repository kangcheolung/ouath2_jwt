package com.cotato.backend.oauth.controller;

import com.cotato.backend.common.dto.DataResponse;
import com.cotato.backend.common.exception.AppException;
import com.cotato.backend.common.exception.ErrorCode;
import com.cotato.backend.common.jwt.JwtTokenProvider;
import com.cotato.backend.common.jwt.TokenBlacklistService;
import com.cotato.backend.domain.user.entity.User;
import com.cotato.backend.domain.user.repository.UserRepository;
import com.cotato.backend.oauth.dto.LogoutRequest;
import com.cotato.backend.oauth.dto.TokenResponse;
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