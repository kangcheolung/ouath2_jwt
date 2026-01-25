package com.cotato.backend.common.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:";

    /**
     * 토큰을 블랙리스트에 추가
     * @param token JWT 토큰
     * @param expirationTimeInMs 토큰 만료까지 남은 시간 (밀리초)
     */
    public void addToBlacklist(String token, long expirationTimeInMs) {
        String key = BLACKLIST_PREFIX + token;
        // 토큰 만료 시간까지만 블랙리스트에 보관 (만료되면 어차피 무효)
        redisTemplate.opsForValue().set(key, "logout", expirationTimeInMs, TimeUnit.MILLISECONDS);
        log.info("토큰이 블랙리스트에 추가되었습니다.");
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     * @param token JWT 토큰
     * @return 블랙리스트에 있으면 true
     */
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
