package com.seasonthon.everflow.app.memo.controller;

import com.seasonthon.everflow.app.global.code.dto.ApiResponse;
import com.seasonthon.everflow.app.global.oauth.domain.CustomUserDetails;
import com.seasonthon.everflow.app.memo.dto.MemoDto;
import com.seasonthon.everflow.app.memo.dto.UpdateMemoRequestDto;
import com.seasonthon.everflow.app.memo.service.MemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/memo")
@Tag(name = "Memo API", description = "메모 관련 API")
public class MemoController {

    private final MemoService memoService;

    // 로그인한 사용자의 가족 메모 조회 (가족당 1장, 항상 존재)
    @Operation(summary = "우리 가족 메모 조회", description = "로그인한 사용자가 속한 가족의 메모를 조회합니다. " + "가족당 1장만 존재하며, 조회 시 메모가 없으면 자동으로 생성됩니다.")
    @GetMapping
    public ApiResponse<MemoDto> getMemo(@AuthenticationPrincipal CustomUserDetails me) {
        return ApiResponse.onSuccess(
                memoService.getOrCreate(me.getUserId())
        );
    }

    @Operation(summary = "우리 가족 메모 수정", description = "로그인한 사용자가 속한 가족의 메모 본문을 수정합니다. " + "버전은 서버에서 자동 관리되며, 최종 본문 길이는 최대 800자입니다.")
    @PatchMapping
    public ApiResponse<MemoDto> updateMemo(@AuthenticationPrincipal CustomUserDetails me,
                                           @RequestBody @Valid UpdateMemoRequestDto req) {
        return ApiResponse.onSuccess(
                memoService.update(me.getUserId(), req.content())
        );
    }
}
