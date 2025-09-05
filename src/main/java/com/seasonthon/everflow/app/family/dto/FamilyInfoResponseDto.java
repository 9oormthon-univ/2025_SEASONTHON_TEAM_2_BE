package com.seasonthon.everflow.app.family.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FamilyInfoResponseDto {
    private String familyCode;
    private String familyName;
    private String verificationQuestion;
    private String verificationAnswer;
}
