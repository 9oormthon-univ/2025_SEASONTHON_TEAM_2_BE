package com.seasonthon.everflow.app.bookshelf.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/* 특정 사용자의 책장 화면 응답 */
public record BookshelfUserViewDto(
        @Schema(description = "사용자 ID", example = "42")
        Long userId,

        @Schema(description = "닉네임", example = "아빠")
        String nickname,

        @Schema(description = "이 사용자의 책장 최종 수정 시각")
        LocalDateTime lastUpdatedAt,

        @Schema(description = "책장 항목 목록(질문+답변)")
        List<BookshelfEntryDto> items
) {}
