package com.seasonthon.everflow.app.bookshelf.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public class BookshelfDto {

    // 내 책장/가족 책장 공통 응답: 질문 + 해당 사용자의 답변
    public record ShelfItem(
            Long questionId,
            String questionText,
            String answer                // null 허용
    ) {}

    // 특정 사용자의 책장 응답
    public record UserShelfResponse(
            Long userId,
            String nickname,
            java.time.LocalDateTime lastUpdatedAt, // 책장 전체의 마지막 저장/수정 시각
            java.util.List<ShelfItem> items
    ) {}

    // 내 답변 일괄 저장/수정 요청
    public record WriteAnswersRequest(
            @Schema(description = "질문-답 세트 목록(15개)", required = true)
            List<AnswerPair> items
    ) {
        public record AnswerPair(
                @Schema(description = "질문 ID(1~15 순번 ID)", example = "1")
                Long questionId,
                @Schema(description = "답변(null 가능)", example = "나의 답변")
                String answer
        ) {}
    }
}