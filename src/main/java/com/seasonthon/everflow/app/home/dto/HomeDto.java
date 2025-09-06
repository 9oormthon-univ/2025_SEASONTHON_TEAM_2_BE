package com.seasonthon.everflow.app.home.dto;

public class HomeDto {

    /** 가족 친밀도(참여율) 응답 DTO */
    public record ClosenessResponse(
            int percentage,
            long myCount,
            long familyMaxCount,
            int myRank,
            Long familyId
    ) {}
}