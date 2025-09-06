package com.seasonthon.everflow.app.family.dto;

import com.seasonthon.everflow.app.global.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JoinAttemptResponseDto {
    private final boolean correct; // 정답 여부
    private final boolean exceeded; // 4회 이상 오답 여부
    private final BaseErrorCode status;
}
