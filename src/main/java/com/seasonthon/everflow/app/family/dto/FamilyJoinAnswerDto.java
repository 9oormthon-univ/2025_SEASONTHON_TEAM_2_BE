package com.seasonthon.everflow.app.family.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FamilyJoinAnswerDto {
    @NotBlank(message = "초대 코드는 필수 입력 항목입니다.")
    private String inviteCode;

    @NotBlank(message = "가족 검증 답변은 필수 입력 항목입니다.")
    @Size(max = 8, message = "답변은 최대 8글자까지 가능합니다.")
    private String verificationAnswer;
}
