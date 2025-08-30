package com.seasonthon.everflow.app.global.oauth.controller;

import com.seasonthon.everflow.app.global.code.dto.ApiResponse;
import com.seasonthon.everflow.app.global.code.dto.LoginResponseDto;
import com.seasonthon.everflow.app.global.oauth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
