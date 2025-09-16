package com.seasonthon.everflow.app.home.dto;

public record ClosenessResponseDto(
        int percentage,
        long myCount,
        long familyMaxCount,
        int myRank,
        Long familyId
) {}