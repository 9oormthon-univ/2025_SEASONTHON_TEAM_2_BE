package com.seasonthon.everflow.app.bookshelf.controller;

import com.seasonthon.everflow.app.bookshelf.dto.BookshelfDto;
import com.seasonthon.everflow.app.bookshelf.service.BookshelfService;
import com.seasonthon.everflow.app.global.code.dto.ApiResponse;
import com.seasonthon.everflow.app.global.oauth.domain.CustomUserDetails;
import com.seasonthon.everflow.app.global.oauth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookshelf")
@RequiredArgsConstructor
@Tag(name = "Bookshelf", description = "가족 책장 API")
public class BookshelfController {

    private final BookshelfService bookshelfService;
    private final AuthService authService;

    // 내 책장 조회
    @Operation(summary = "내 책장 조회", description = "기본 질문 15개 + 내 답변을 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<BookshelfDto.UserShelfResponse> getMyShelf(@AuthenticationPrincipal CustomUserDetails me) {
        Long meId = authService.getUserId(me);
        return ApiResponse.onSuccess(bookshelfService.getMyShelf(meId));
    }

    // 특정 사용자 책장 조회 (가족만)
    @Operation(summary = "가족 책장 조회", description = "같은 가족 구성원의 책장을 조회합니다.")
    @GetMapping("/{userId}")
    public ApiResponse<BookshelfDto.UserShelfResponse> getUserShelf(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long userId
    ) {
        Long meId = authService.getUserId(me);
        return ApiResponse.onSuccess(bookshelfService.getUserShelf(meId, userId));
    }

    // 내 답변 저장/수정 (일괄)
    @Operation(
            summary = "내 답변 저장/수정 (일괄)",
            description = "질문별로 answer가 null이어도 저장됩니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "bulk-answers",
                                    value = """
                {
                  "items": [
                    {"questionId": 1, "answer": ""},
                    {"questionId": 2, "answer": ""},
                    {"questionId": 3, "answer": ""},
                    {"questionId": 4, "answer": ""},
                    {"questionId": 5, "answer": ""},
                    {"questionId": 6, "answer": ""},
                    {"questionId": 7, "answer": ""},
                    {"questionId": 8, "answer": ""},
                    {"questionId": 9,  "answer": ""},
                    {"questionId": 10, "answer": ""},
                    {"questionId": 11, "answer": ""},
                    {"questionId": 12, "answer": ""},
                    {"questionId": 13, "answer": ""},
                    {"questionId": 14, "answer": ""},
                    {"questionId": 15, "answer": ""}
                  ]
                }
                """
                            )
                    )
            )
    )
    @PatchMapping("/bookshelf/me")
    public ApiResponse<Void> writeMyAnswers(
            @AuthenticationPrincipal CustomUserDetails me,
            @RequestBody BookshelfDto.WriteAnswersRequest req
    ) {
        Long meId = authService.getUserId(me);
        bookshelfService.writeMyAnswers(meId, req);
        return ApiResponse.onSuccess(null);
    }
}