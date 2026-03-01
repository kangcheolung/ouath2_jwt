package com.cotato.backend.common.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    private static final String REFRESH_TOKEN_PREFIX = "refresh:";

    /**
     * Refresh Token을 Redis에 저장 (화이트리스트 등록)
     * 로그인 및 토큰 갱신 시 호출
     */
    public void save(Long userId, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, refreshToken, refreshTokenValidity, TimeUnit.MILLISECONDS);
        log.info("Refresh Token Redis 저장 완료 - userId: {}", userId);
    }

    /**
     * Redis에 저장된 Refresh Token이 요청 토큰과 일치하는지 검증
     * 불일치 시 탈취 또는 이미 사용된 토큰으로 간주
     */
    public boolean validate(Long userId, String refreshToken) {
        String storedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
        return storedToken != null && storedToken.equals(refreshToken);
    }

    /**
     * Refresh Token을 Redis에서 삭제 (화이트리스트 제거)
     * 로그아웃 시 호출 → 이후 해당 Refresh Token으로 갱신 불가
     */
    public void delete(Long userId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
        log.info("Refresh Token Redis 삭제 완료 - userId: {}", userId);
    }
}
