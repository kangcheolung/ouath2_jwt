package com.cotato.backend.oauth.controller;

import com.cotato.backend.common.dto.DataResponse;
import com.cotato.backend.common.jwt.JwtTokenProvider;
import com.cotato.backend.domain.user.entity.User;
import com.cotato.backend.domain.user.repository.UserRepository;
import com.cotato.backend.oauth.dto.TokenResponse;
import com.cotato.backend.user.dto.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class OAuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    /**
     * 현재 인증된 사용자 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<DataResponse<UserInfoResponse>> getCurrentUser(
            @AuthenticationPrincipal User user) {

        UserInfoResponse userInfo = UserInfoResponse.from(user);
        return ResponseEntity.ok(DataResponse.from(userInfo));
    }

    /**
     * Refresh Token으로 Access Token 재발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<DataResponse<TokenResponse>> refresh(
            @RequestHeader("Authorization") String refreshToken) {

        // Bearer 제거
        String token = refreshToken.replace("Bearer ", "");

        // Refresh Token 검증
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).build();
        }

        // User ID 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 새로운 Access Token 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        TokenResponse tokenResponse = TokenResponse.of(newAccessToken, newRefreshToken);
        return ResponseEntity.ok(DataResponse.from(tokenResponse));
    }

    /**
     * 토큰 검증 (테스트용)
     */
    @GetMapping("/validate")
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
}