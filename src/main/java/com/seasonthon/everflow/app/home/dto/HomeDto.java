package com.seasonthon.everflow.app.home.dto;

public class HomeDto {

    /** 가족 친밀도(참여율) 응답 DTO */
    public record ClosenessResponse(
            int percentage,        // 0~100
            long myCount,          // 로그인 사용자의 답변 수
            long familyMaxCount,   // 가족 내 최대 답변 수(사용자 단위)
            int myRank,            // 1부터 시작
            Long familyId,
            String scope           // "ALL" | "ACTIVE" | "LAST_30D"
    ) {}
}