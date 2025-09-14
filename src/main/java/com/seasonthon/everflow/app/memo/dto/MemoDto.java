package com.seasonthon.everflow.app.memo.dto;

import java.time.LocalDateTime;

public record MemoDto(
        Long id,
        Long familyId,
        String content,
        int version,
        LocalDateTime updatedAt
) {}