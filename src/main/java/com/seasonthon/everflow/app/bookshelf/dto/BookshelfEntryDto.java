package com.seasonthon.everflow.app.bookshelf.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/* 책장 항목: 질문 + (해당 사용자 기준) 답변 */
public record BookshelfEntryDto(
        @Schema(description = "질문 ID", example = "1")
        Long questionId,

        @Schema(description = "질문 내용", example = "우리 가족만의 인사말은?")
        String questionText,

        @Schema(description = "사용자의 답변(없을 수 있음)", example = "매일 저녁 '잘 먹겠습니다'")
        String answer
) {}
