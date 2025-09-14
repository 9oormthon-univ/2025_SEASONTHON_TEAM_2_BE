package com.seasonthon.everflow.app.memo.dto;

import com.seasonthon.everflow.app.memo.domain.Memo;

public class MemoMapper {
    public static MemoDto toDto(Memo memo) {
        return new MemoDto(
                memo.getId(),
                memo.getFamilyId(),
                memo.getContent(),
                memo.getVersion(),
                memo.getUpdatedAt()
        );
    }
}
