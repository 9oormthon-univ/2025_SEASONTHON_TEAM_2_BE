package com.seasonthon.everflow.app.bookshelf.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/* 내 답변을 여러 개 한 번에 저장/수정(업서트)하는 요청 DTO */
public record BookshelfAnswersUpsertRequestDto(
        @Schema(description = "질문-답 세트 목록(최대 15개)", required = true)
        List<ItemDto> items
) {
    /* 질문-답 1쌍 DTO */
    public record ItemDto(
            @Schema(description = "질문 ID(PK, 기본:1~15 / 커스텀: DB에서 생성된 값)", example = "1")
            Long questionId,

            @Schema(description = "답변(없으면 null 가능)", example = "우리 가족 구호는 '화이팅'")
            String answer
    ) {}
}