package com.seasonthon.everflow.app.global.oauth.service;

import com.seasonthon.everflow.app.global.code.dto.LoginResponseDto;
import com.seasonthon.everflow.app.global.code.dto.UserInfoResponseDto;
import com.seasonthon.everflow.app.global.code.status.ErrorStatus;
import com.seasonthon.everflow.app.global.exception.GeneralException;
import com.seasonthon.everflow.app.global.oauth.domain.CustomUserDetails;
import com.seasonthon.everflow.app.global.security.JwtService;
import com.seasonthon.everflow.app.global.security.TokenBlacklistService;
import com.seasonthon.everflow.app.user.domain.User;
import com.seasonthon.everflow.app.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
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
    private final TokenBlacklistService tokenBlacklistService;

    @Transactional
    public LoginResponseDto login(String idToken) {
        // ID 토큰을 검증하고 사용자 정보(CustomUserDetails)를 가져옵니다.
        CustomUserDetails userDetails = idTokenService.loadUserByIdToken(idToken);
        String email = userDetails.getUsername();
        Long userId = userDetails.getUserId();
        String roleType = userDetails.getRoleType().toString();

        // 우리 서비스의 Access/Refresh 토큰을 생성합니다.
        String accessToken = jwtService.createAccessToken(email, userId, roleType);
        String refreshToken = jwtService.createRefreshToken();

        // Refresh 토큰을 데이터베이스에 저장합니다.
        jwtService.updateRefreshToken(email, refreshToken);

        // 생성된 토큰들을 반환합니다.
        return new LoginResponseDto(accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public UserInfoResponseDto getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        String familyCode = null;
        if (user.getFamily() != null) {
            familyCode = user.getFamily().getInviteCode();
        }

        return new UserInfoResponseDto(
                user.getEmail(),
                user.getNickname(),
                user.getProfileUrl(),
                user.getRoleType().toString(),
                familyCode
        );
    }

    @Transactional
    public void logout(HttpServletRequest request) {
        String accessToken = jwtService.extractAccessToken(request)
                .orElseThrow(() -> new GeneralException(ErrorStatus.INVALID_TOKEN));

        tokenBlacklistService.blacklistToken(accessToken);
    }
}
