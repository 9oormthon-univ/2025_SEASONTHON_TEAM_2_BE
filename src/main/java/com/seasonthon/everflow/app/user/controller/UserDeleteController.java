package com.seasonthon.everflow.app.user.controller;

import com.seasonthon.everflow.app.global.code.dto.ApiResponse;
import com.seasonthon.everflow.app.global.oauth.domain.CustomUserDetails;
import com.seasonthon.everflow.app.user.service.UserDeleteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User OIDC Login", description = "OIDC Login API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserDeleteController {

    private final UserDeleteService userDeleteService;


    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 유저와 관련된 모든 데이터를 삭제합니다.")
    @DeleteMapping("/withdraw")
    public ApiResponse<Void> withdraw(@AuthenticationPrincipal CustomUserDetails me) {
        Long userId = userDeleteService.getUserId(me);
        userDeleteService.withdrawUser(userId);
        return ApiResponse.onSuccess(null);
    }
}
