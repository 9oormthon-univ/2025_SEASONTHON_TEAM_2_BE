package com.seasonthon.everflow.app.bookshelf.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/* 가족 커스텀 질문 생성 요청 DTO */
public record CustomBookshelfQuestionCreateRequestDto(
        @Schema(description = "질문 내용", example = "우리 가족의 좌우명은?")
        @NotBlank
        @Size(max = 500)
        String question
) {}