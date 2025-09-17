package com.seasonthon.everflow.app.home.dto;

import java.util.List;

public record FamilySummaryResponseDto(
        List<FamilyMemberSummary> members
) {

    public record FamilyMemberSummary(
            Long userId,
            String nickname,
            String shelfColor
    ) {}
}
