package com.seasonthon.everflow.app.user.controller;

import com.seasonthon.everflow.app.global.code.dto.ApiResponse;
import com.seasonthon.everflow.app.global.oauth.domain.CustomUserDetails;
import com.seasonthon.everflow.app.user.dto.ProfileImageUploadDto;
import com.seasonthon.everflow.app.user.dto.UserNicknameUpdateDto;
import com.seasonthon.everflow.app.user.dto.UserProfileImageResponseDto;
import com.seasonthon.everflow.app.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "닉네임 수정", description = "로그인 사용자의 닉네임 수정, 최대 5자, 앞뒤 공백 자동제거")
    @PatchMapping("users/me/nickname")
    public ApiResponse<Void> updateNickname(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserNicknameUpdateDto request) {
        if (userDetails == null) {
            return ApiResponse.onFailure("AUTH401", "인증 정보가 없습니다.", null);
        }
        userService.updateNickname(userDetails.getUserId(), request);
        return ApiResponse.onSuccess(null);
    }

    @Operation(
            summary = "프로필 이미지 업로드",
            description = "로그인 사용자의 프로필 이미지를 업로드하고 URL을 갱신합니다."
    )
    @PatchMapping(value = "users/me/profileImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UserProfileImageResponseDto> updateProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute ProfileImageUploadDto request
    ) {
        if (userDetails == null) {
            return ApiResponse.onFailure("AUTH401", "인증 정보가 없습니다.", null);
        }
        UserProfileImageResponseDto res = userService.updateProfileImage(userDetails.getUserId(), request.getFile());
        return ApiResponse.onSuccess(res);
    }
}
