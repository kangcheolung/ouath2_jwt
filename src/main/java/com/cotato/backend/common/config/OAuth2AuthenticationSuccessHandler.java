package com.cotato.backend.common.config;

import com.cotato.backend.common.jwt.JwtTokenProvider;
import com.cotato.backend.domain.user.entity.CustomOAuth2User;
import com.cotato.backend.domain.user.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = oAuth2User.getUser();

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        log.info("OAuth2 로그인 성공 - User ID: {}, Email: {}", user.getId(), user.getEmail());

        // 프로필에 따라 리다이렉트 URL 결정
        String frontendUrl;
        if ("prod".equals(activeProfile)) {
            frontendUrl = "https://your-frontend.com";  // 배포 시 실제 프론트엔드 URL로 변경
        } else {
            frontendUrl = "http://localhost:3000";
        }

        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl)
            .queryParam("accessToken", accessToken)
            .queryParam("refreshToken", refreshToken)
            .build()
            .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}