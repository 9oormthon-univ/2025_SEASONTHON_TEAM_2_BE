package com.seasonthon.everflow.app.global.oauth.service;

import com.seasonthon.everflow.app.global.code.dto.LoginResponseDto;
import com.seasonthon.everflow.app.global.oauth.domain.CustomUserDetails;
import com.seasonthon.everflow.app.global.security.JwtService;
import com.seasonthon.everflow.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final IdTokenService idTokenService;
    private final JwtService jwtService;

    @Transactional
    public LoginResponseDto login(String idToken) {
        // 1. ID 토큰을 검증하고 사용자 정보(CustomUserDetails)를 가져옵니다.
        CustomUserDetails userDetails = idTokenService.loadUserByIdToken(idToken);
        String email = userDetails.getUsername();
        Long userId = userDetails.getUserId();

        // 2. 우리 서비스의 Access/Refresh 토큰을 생성합니다.
        String accessToken = jwtService.createAccessToken(email, userId, userDetails.getRoleType().toString());
        String refreshToken = jwtService.createRefreshToken();

        // 3. Refresh 토큰을 데이터베이스에 저장합니다.
        jwtService.updateRefreshToken(email, refreshToken);

        // 4. 생성된 토큰들을 반환합니다.
        return new LoginResponseDto(accessToken, refreshToken);
    }
}