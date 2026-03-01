package com.cotato.backend.oauth.service;

import com.cotato.backend.common.jwt.JwtTokenProvider;
import com.cotato.backend.common.jwt.RefreshTokenService;
import com.cotato.backend.domain.oauth.OAuth2Profile;
import com.cotato.backend.domain.user.entity.User;
import com.cotato.backend.domain.user.repository.UserRepository;
import com.cotato.backend.oauth.dto.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    /**
     * OAuth 로그인 공통 처리: 유저 조회/생성 → JWT 발급 → Refresh Token Redis 저장
     */
    public TokenResponse processLogin(OAuth2Profile profile) {
        User user = userRepository.findByEmailAndProvider(profile.getEmail(), profile.getProvider())
            .orElseGet(() -> {
                User newUser = profile.toUser();
                return userRepository.save(newUser);
            });

        log.info("사용자 로그인 성공 - userId: {}, email: {}", user.getId(), user.getEmail());

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        refreshTokenService.save(user.getId(), refreshToken);

        return TokenResponse.of(accessToken, refreshToken);
    }
}
