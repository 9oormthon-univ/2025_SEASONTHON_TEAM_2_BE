package com.seasonthon.everflow.app.family.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FamilyCreateRequestDto {
    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(max = 5, message = "닉네임은 최대 5글자까지 가능합니다.")
    private String nickname;

    @NotBlank(message = "가족명은 필수 입력 항목입니다.")
    @Size(max = 8, message = "가족명은 최대 8글자까지 가능합니다.")
    private String familyName;

    @NotBlank(message = "가족 검증 질문은 필수 입력 항목입니다.")
    @Size(max = 20, message = "질문은 최대 20글자까지 가능합니다.")
    private String verificationQuestion;

    @NotBlank(message = "가족 검증 답변은 필수 입력 항목입니다.")
    @Size(max = 8, message = "답변은 최대 8글자까지 가능합니다.")
    private String verificationAnswer;
}
