package com.seasonthon.everflow.app.family.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class FamilyMembersResponseDto {

    private String familyName;
    private boolean creator;
    private List<MemberInfo> members;

    @Getter
    @AllArgsConstructor
    public static class MemberInfo {
        private String nickname;
        private String profileUrl;
    }
}
