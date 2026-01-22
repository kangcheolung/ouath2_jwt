package com.cotato.backend.oauth.controller;

import com.cotato.backend.common.dto.DataResponse;
import com.cotato.backend.common.jwt.JwtTokenProvider;
import com.cotato.backend.domain.user.entity.User;
import com.cotato.backend.domain.user.repository.UserRepository;
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

    // 인증된 현재 사용자 정보 조회
    @GetMapping("/me")
    @Operation(summary = "현재 사용자 정보 조회")
    public ResponseEntity<DataResponse<UserInfoResponse>> getCurrentUser(
        @Parameter(hidden = true) @AuthenticationPrincipal User user) {

        UserInfoResponse userInfo = UserInfoResponse.from(user);
        return ResponseEntity.ok(DataResponse.from(userInfo));
    }

    // Token 갱신
    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신")
    @SecurityRequirement(name = "accessTokenAuth")
    public ResponseEntity<DataResponse<TokenResponse>> refresh(
        @RequestHeader("Authorization") String refreshToken) {

        String token = refreshToken.replace("Bearer ", "");

        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).build();
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

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
}