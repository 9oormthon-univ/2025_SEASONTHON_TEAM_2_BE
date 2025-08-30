package com.seasonthon.everflow.app.global.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {

    private final Cache<String, String> tokenBlacklist;

    public TokenBlacklistService(@Value("${jwt.refresh.expiration}") Long refreshTokenExpirationPeriod) {
        this.tokenBlacklist = Caffeine.newBuilder()
                .expireAfterWrite(refreshTokenExpirationPeriod, TimeUnit.MILLISECONDS)
                .build();
    }

    /**
     * 토큰을 블랙리스트에 추가합니다.
     * @param token 무효화할 토큰
     */
    public void blacklistToken(String token) {
        if (token != null && !token.isEmpty()) {
            tokenBlacklist.put(token, "blacklisted");
        }
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인합니다.
     * @param token 확인할 토큰
     * @return 블랙리스트에 있으면 true
     */
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklist.getIfPresent(token) != null;
    }
}
