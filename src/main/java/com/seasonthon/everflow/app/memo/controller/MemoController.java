package com.seasonthon.everflow.app.memo.controller;

import com.seasonthon.everflow.app.global.code.dto.ApiResponse;
import com.seasonthon.everflow.app.global.oauth.domain.CustomUserDetails;
import com.seasonthon.everflow.app.memo.dto.MemoDto;
import com.seasonthon.everflow.app.memo.dto.UpdateMemoRequestDto;
import com.seasonthon.everflow.app.memo.service.MemoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/memo")
public class MemoController {

    private final MemoService memoService;

    /* 로그인한 사용자의 가족 메모 조회 (가족당 1장, 항상 존재) */
    @GetMapping
    public ApiResponse<MemoDto> getMemo(@AuthenticationPrincipal CustomUserDetails me) {
        return ApiResponse.onSuccess(
                memoService.getOrCreate(me.getUserId())
        );
    }

    /* 메모 수정 (낙관적 락: version 필요) */
    @PutMapping
    public ApiResponse<MemoDto> updateMemo(@AuthenticationPrincipal CustomUserDetails me,
                                           @RequestBody @Valid UpdateMemoRequestDto req) {
        return ApiResponse.onSuccess(
                memoService.update(me.getUserId(), req.version(), req.content())
        );
    }
}