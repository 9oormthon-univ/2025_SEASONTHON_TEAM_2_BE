package com.seasonthon.everflow.app.family.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class FamilyEditRequestDto {
    @Nullable
    @Size(min = 1, max = 8)
    private String familyName;

    @Nullable
    @Size(min = 1, max = 20)
    private String verificationQuestion;

    @Nullable
    @Size(min = 1, max = 8)
    private String verificationAnswer;

    public boolean isAllEmpty() {
        return (familyName == null || familyName.isBlank())
                && (verificationQuestion == null || verificationQuestion.isBlank())
                && (verificationAnswer == null || verificationAnswer.isBlank());
    }
}
