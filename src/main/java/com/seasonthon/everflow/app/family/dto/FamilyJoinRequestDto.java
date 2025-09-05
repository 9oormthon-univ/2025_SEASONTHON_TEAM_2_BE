package com.seasonthon.everflow.app.family.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FamilyJoinRequestDto {
    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(max = 5, message = "닉네임은 최대 5글자까지 가능합니다.")
    private String nickname;

    @NotBlank(message = "초대 코드는 필수 입력 항목입니다.")
    private String inviteCode;
}
