package com.seasonthon.everflow.app.family.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FamilyCreateRequestDto {
    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Schema(description = "본인 닉네임", example = "김철수")
    @Size(max = 5, message = "닉네임은 최대 5글자까지 가능합니다.")
    private String nickname;

    @NotBlank(message = "가족명은 필수 입력 항목입니다.")
    @Schema(description = "가족명 설정", example = "햄볶는 우리집")
    @Size(max = 8, message = "가족명은 최대 8글자까지 가능합니다.")
    private String familyName;

    @NotBlank(message = "가족 검증 질문은 필수 입력 항목입니다.")
    @Schema(description = "가족 검증 질문", example = "우리집 강아지 이름은?")
    @Size(max = 20, message = "질문은 최대 20글자까지 가능합니다.")
    private String verificationQuestion;

    @NotBlank(message = "가족 검증 답변은 필수 입력 항목입니다.")
    @Schema(description = "가족 검증 답변", example = "열무")
    @Size(max = 8, message = "답변은 최대 8글자까지 가능합니다.")
    private String verificationAnswer;
}
