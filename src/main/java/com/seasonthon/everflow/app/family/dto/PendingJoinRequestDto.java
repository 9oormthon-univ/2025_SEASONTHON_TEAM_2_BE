package com.seasonthon.everflow.app.family.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PendingJoinRequestDto {
    private Long requestId;
    private Long userId;
    private String nickname;
    private int attempts;
}
