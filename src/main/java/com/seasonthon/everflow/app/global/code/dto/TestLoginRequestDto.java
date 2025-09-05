package com.seasonthon.everflow.app.global.code.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TestLoginRequestDto {

    @Schema(description = "테스트 유저 이메일(임의 설정)", example = "apple_test@example.com")
    private String email;

    @Schema(description = "닉네임 (5자 이내)", example = "닉네임입력")
    private String nickname;
}
