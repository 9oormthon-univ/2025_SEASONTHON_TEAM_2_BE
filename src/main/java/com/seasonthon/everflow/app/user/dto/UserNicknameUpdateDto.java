package com.seasonthon.everflow.app.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserNicknameUpdateDto {
    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Schema(description = "수정할 닉네임", example = "홍길동")
    @Size(max = 5, message = "닉네임은 최대 5글자까지 가능합니다.")
    private String nickname;
}
