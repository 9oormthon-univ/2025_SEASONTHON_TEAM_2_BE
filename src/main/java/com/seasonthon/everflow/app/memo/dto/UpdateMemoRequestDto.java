package com.seasonthon.everflow.app.memo.dto;

import jakarta.validation.constraints.Size;

public record UpdateMemoRequestDto(
        @Size(max = 800) String content
) {}
