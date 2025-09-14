package com.seasonthon.everflow.app.memo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateMemoRequestDto(
        @NotNull Integer version,
        @Size(max = 800) String content
) {}