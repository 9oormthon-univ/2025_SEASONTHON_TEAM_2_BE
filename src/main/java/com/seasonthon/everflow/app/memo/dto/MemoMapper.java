package com.seasonthon.everflow.app.memo.dto;

import com.seasonthon.everflow.app.memo.domain.Memo;

public class MemoMapper {

    public static MemoDto toDto(Memo memo) {
        String updatedByNickname = (memo.getUpdatedBy() != null)
                ? memo.getUpdatedBy().getNickname()
                : "탈퇴 유저";

        return new MemoDto(
                memo.getId(),
                memo.getFamilyId(),
                memo.getContent(),
                updatedByNickname,
                memo.getVersion(),
                memo.getUpdatedAt()
        );
    }
}
