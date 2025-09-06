package com.seasonthon.everflow.app.global.code.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoResponseDto {
    private Long userId;
    private String email;
    private String nickname;
    private String profileUrl;
    private String role;
    private String familyCode;
}
