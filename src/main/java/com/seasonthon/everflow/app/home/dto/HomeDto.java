package com.seasonthon.everflow.app.home.dto;

public class HomeDto {

    /** 가족 친밀도(참여율) 응답 DTO */
    public record ClosenessResponse(
            int percentage,        // 내 참여율 (0~100)
            long myCount,          // 최근 30일 내 내가 남긴 답변 수
            long familyMaxCount,   // 같은 기간 가족 중 최대 답변 수
            int myRank,            // 내 랭크 (동률 처리 기준은 서비스 로직 참조)
            Long familyId          // 가족 ID
    ) {}

    /** 가족 책장 목록(나 포함) DTO */
    public record FamilyMemberSummary(
            Long userId,
            String nickname,
            String shelfColor
    ) {}

    public record FamilySummaryResponse(
            java.util.List<FamilyMemberSummary> members
    ) {}
}