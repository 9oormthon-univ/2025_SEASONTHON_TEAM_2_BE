package com.seasonthon.everflow.app.global.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.seasonthon.everflow.app.global.code.status.ErrorStatus;
import com.seasonthon.everflow.app.global.exception.GeneralException;
import com.seasonthon.everflow.app.user.domain.User;
import com.seasonthon.everflow.app.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Getter
@Slf4j
public class JwtService {

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.access.expiration}")
    private Long accessTokenExpirationPeriod;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenExpirationPeriod;

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    private final UserRepository userRepository;
    private final TokenBlacklistService tokenBlacklistService;

    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String EMAIL_CLAIM = "email";
    private static final String USERID_CLAIM = "userId";
    private static final String ROLE_CLAIM = "role";
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Access Token을 생성합니다.
     */
    public String createAccessToken(String email, Long userId, String roleType) {
        Date now = new Date();
        return JWT.create()
                .withSubject(ACCESS_TOKEN_SUBJECT)
                .withExpiresAt(new Date(now.getTime() + accessTokenExpirationPeriod))
                .withClaim(EMAIL_CLAIM, email)
                .withClaim(USERID_CLAIM, userId)
                .withClaim(ROLE_CLAIM, roleType)
                .sign(Algorithm.HMAC512(secretKey));
    }

    /**
     * Refresh Token을 생성합니다.
     */
    public String createRefreshToken() {
        Date now = new Date();
        return JWT.create()
                .withSubject(REFRESH_TOKEN_SUBJECT)
                .withExpiresAt(new Date(now.getTime() + refreshTokenExpirationPeriod))
                .sign(Algorithm.HMAC512(secretKey));
    }

    /**
     * Access Token을 HTTP 응답 헤더에 설정합니다.
     */
    public void setAccessTokenHeader(HttpServletResponse response, String accessToken){
        response.setHeader(accessHeader, BEARER_PREFIX + accessToken);
    }

    /**
     * Refresh Token을 HTTP 응답 헤더에 설정합니다.
     */
    public void setRefreshTokenHeader(HttpServletResponse response, String refreshToken) {
        response.setHeader(refreshHeader, BEARER_PREFIX + refreshToken);
    }

    /**
     * Access Token과 Refresh Token을 HTTP 응답 헤더에 담아 전송합니다.
     */
    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setStatus(HttpServletResponse.SC_OK);
        setAccessTokenHeader(response, accessToken);
        setRefreshTokenHeader(response, refreshToken);
    }

    /**
     * "Bearer "를 제외한 실제 토큰 값을 추출합니다.
     */
    public Optional<String> extractToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return Optional.of(bearerToken.replace(BEARER_PREFIX, ""));
        }
        return Optional.empty();
    }

    /**
     * Refresh Token을 HTTP 요청 헤더에서 추출합니다.
     */
    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(refreshHeader))
                .flatMap(this::extractToken);
    }

    /**
     * Access Token을 HTTP 요청 헤더에서 추출합니다.
     */
    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessHeader))
                .flatMap(this::extractToken);
    }

    /**
     * Access Token에서 이메일을 추출하고 유효성을 검증합니다.
     */
    public Optional<String> extractEmail(String accessToken) {
        try {
            return Optional.ofNullable(JWT.require(Algorithm.HMAC512(secretKey))
                    .build()
                    .verify(accessToken)
                    .getClaim(EMAIL_CLAIM)
                    .asString());
        } catch (Exception e) {
            log.error("유효하지 않은 Access Token 입니다. {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Transactional
    public void updateRefreshToken(String email, String refreshToken) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        user.updateRefreshToken(refreshToken);
    }

    public boolean isTokenValid(String token) {
        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            return false;
        }
        try {
            JWT.require(Algorithm.HMAC512(secretKey)).build().verify(token);
            return true;
        } catch (Exception e) {
            log.warn("유효하지 않은 토큰입니다. 원인: {}", e.getMessage());
            return false;
        }
    }

    public String verifyTokenAndGetEmail(String token) {
        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            throw new GeneralException(ErrorStatus.INVALID_TOKEN);
        }
        try {
            return JWT.require(Algorithm.HMAC512(secretKey))
                    .build()
                    .verify(token)
                    .getClaim(EMAIL_CLAIM)
                    .asString();
        } catch (TokenExpiredException e) {
            log.warn("만료된 토큰입니다. {}", e.getMessage());
            throw new GeneralException(ErrorStatus.EXPIRED_TOKEN);
        } catch (JWTVerificationException e) {
            log.warn("유효하지 않은 토큰입니다. {}", e.getMessage());
            throw new GeneralException(ErrorStatus.INVALID_TOKEN);
        }
    }
}