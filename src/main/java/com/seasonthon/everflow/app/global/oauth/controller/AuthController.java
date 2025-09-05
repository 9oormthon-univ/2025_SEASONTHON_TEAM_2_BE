package com.seasonthon.everflow.app.global.oauth.controller;

import com.seasonthon.everflow.app.global.code.dto.ApiResponse;
import com.seasonthon.everflow.app.global.code.dto.LoginResponseDto;
import com.seasonthon.everflow.app.global.code.dto.TestLoginRequestDto;
import com.seasonthon.everflow.app.global.code.dto.UserInfoResponseDto;
import com.seasonthon.everflow.app.global.oauth.domain.CustomUserDetails;
import com.seasonthon.everflow.app.global.oauth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User OIDC Login", description = "OIDC Login API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "OIDC 로그인", description = "카카오에서 받은 id_token으로 로그인/회원가입 처리 후 우리 서비스의 토큰을 발급합니다.")
    @PostMapping("/login")
    public ApiResponse<LoginResponseDto> login(@RequestHeader("id_token") String idToken) {
        LoginResponseDto tokens = authService.login(idToken);
        return ApiResponse.onSuccess(tokens);
    }

    @Operation(summary = "내 정보 조회", description = "로그인된 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<UserInfoResponseDto> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ApiResponse.onFailure("AUTH401", "인증 정보가 없습니다.", null);
        }
        UserInfoResponseDto userInfo = authService.getUserInfo(userDetails.getUserId());
        return ApiResponse.onSuccess(userInfo);
    }

    @Operation(summary = "로그아웃", description = "현재 사용자를 로그아웃 처리합니다. (AccessToken 필요)")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        authService.logout(request);
        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "테스트 계정생성", description = "입력한 임의의 이메일을 기반으로 SocialType.APPLE 타입의 Mock 유저를 생성하고 토큰을 발급합니다. 토큰을 잘 기억해두세요. (테스트용)")
    @PostMapping("/test/mock-login")
    public ApiResponse<LoginResponseDto> testAppleLogin(@RequestBody TestLoginRequestDto requestDto) {
        LoginResponseDto tokens = authService.testAppleLogin(requestDto.getEmail(), requestDto.getNickname());
        return ApiResponse.onSuccess(tokens);
    }

    @Operation(summary = "토큰 재발급", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다.")
    @PostMapping("/reissue")
    public ApiResponse<LoginResponseDto> reissue(@RequestHeader("refresh_token") String refreshToken) {
        LoginResponseDto tokens = authService.reissue(refreshToken);
        return ApiResponse.onSuccess(tokens);
    }
}
