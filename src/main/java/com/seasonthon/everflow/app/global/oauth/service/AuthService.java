package com.seasonthon.everflow.app.global.oauth.service;

import com.seasonthon.everflow.app.global.code.dto.LoginResponseDto;
import com.seasonthon.everflow.app.global.code.dto.UserInfoResponseDto;
import com.seasonthon.everflow.app.global.code.status.ErrorStatus;
import com.seasonthon.everflow.app.global.exception.GeneralException;
import com.seasonthon.everflow.app.global.oauth.domain.CustomUserDetails;
import com.seasonthon.everflow.app.global.security.JwtService;
import com.seasonthon.everflow.app.global.security.TokenBlacklistService;
import com.seasonthon.everflow.app.user.domain.RoleType;
import com.seasonthon.everflow.app.user.domain.SocialType;
import com.seasonthon.everflow.app.user.domain.User;
import com.seasonthon.everflow.app.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.UUID;
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

        String familyCode = (user.getFamily() != null) ? user.getFamily().getInviteCode() : null;

        return new UserInfoResponseDto(
                user.getId(),
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

    @Transactional
    public LoginResponseDto testAppleLogin(String email, String nickname) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new GeneralException(ErrorStatus.DUPLICATE_RESOURCE);
        }

        String safeNickname = (nickname != null && !nickname.isBlank()) ? nickname.trim() : "AppleUser";
        if (safeNickname.length() > 5) {
            throw new GeneralException(ErrorStatus.VALIDATION_FAILED);
        }

        User user = User.builder()
                .email(email)
                .nickname(safeNickname)
                .profileUrl("https://www.svgrepo.com/show/452030/avatar-default.svg")
                .oauthId("test-apple-" + UUID.randomUUID())
                .socialType(SocialType.APPLE)
                .roleType(RoleType.ROLE_GUEST)
                .lastLoginAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        String roleType = user.getRoleType().toString();
        String accessToken = jwtService.createAccessToken(user.getEmail(), user.getId(), roleType);
        String refreshToken = jwtService.createRefreshToken();

        jwtService.updateRefreshToken(user.getEmail(), refreshToken);

        return new LoginResponseDto(accessToken, refreshToken);
    }

    @Transactional
    public LoginResponseDto reissue(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new GeneralException(ErrorStatus.MISSING_PARAMETER);
        }

        if (!jwtService.isTokenValid(refreshToken)) {
            throw new GeneralException(ErrorStatus.INVALID_TOKEN);
        }

        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new GeneralException(ErrorStatus.INVALID_TOKEN));

        String newAccessToken = jwtService.createAccessToken(
                user.getEmail(), user.getId(), user.getRoleType().toString()
        );
        String newRefreshToken = jwtService.createRefreshToken();

        jwtService.updateRefreshToken(user.getEmail(), newRefreshToken);

        return new LoginResponseDto(newAccessToken, newRefreshToken);
    }

    /**
     * userId로 familyId 조회 (없으면 예외)
     */
    @Transactional(readOnly = true)
    public Long getFamilyId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        if (user.getFamily() == null) {
            throw new GeneralException(ErrorStatus.FAMILY_NOT_FOUND);
        }
        return user.getFamily().getId();
    }

    /**
     * 인증 객체로 바로 familyId 조회 (미인증/가족없음 예외)
     */
    @Transactional(readOnly = true)
    public Long getFamilyId(CustomUserDetails me) {
        if (me == null) {
            throw new GeneralException(ErrorStatus.AUTH_REQUIRED);
        }
        return getFamilyId(me.getUserId());
    }

    /**
     * 인증 객체에서 userId 반환 (미인증 예외)
     */
    @Transactional(readOnly = true)
    public Long getUserId(CustomUserDetails me) {
        if (me == null) {
            throw new GeneralException(ErrorStatus.AUTH_REQUIRED);
        }
        return me.getUserId();
    }
}
