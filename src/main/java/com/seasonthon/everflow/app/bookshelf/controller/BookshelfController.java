package com.seasonthon.everflow.app.bookshelf.controller;

import com.seasonthon.everflow.app.bookshelf.dto.BookshelfUserViewDto;
import com.seasonthon.everflow.app.bookshelf.dto.BookshelfAnswersUpsertRequestDto;
import com.seasonthon.everflow.app.bookshelf.service.BookshelfService;
import com.seasonthon.everflow.app.global.code.dto.ApiResponse;
import com.seasonthon.everflow.app.global.oauth.domain.CustomUserDetails;
import com.seasonthon.everflow.app.global.oauth.service.AuthService;
import com.seasonthon.everflow.app.bookshelf.dto.BookshelfEntryDto;
import com.seasonthon.everflow.app.bookshelf.dto.CustomBookshelfQuestionCreateRequestDto;
import jakarta.validation.Valid;
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

    @Operation(summary = "내 책장 조회", description = "기본 질문 + 가족 커스텀 질문을 모두 포함해, 각 질문에 대한 나의 답변을 함께 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<BookshelfUserViewDto> getMyShelf(@AuthenticationPrincipal CustomUserDetails me) {
        Long meId = authService.getUserId(me);
        return ApiResponse.onSuccess(bookshelfService.getMyShelf(meId));
    }

    @Operation(summary = "가족 책장 조회", description = "같은 가족 구성원의 책장을 조회합니다. (기본 질문 + 가족 커스텀 질문 전체, 해당 사용자의 답변 포함)")
    @GetMapping("/{userId}")
    public ApiResponse<BookshelfUserViewDto> getUserShelf(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long userId
    ) {
        Long meId = authService.getUserId(me);
        return ApiResponse.onSuccess(bookshelfService.getUserShelf(meId, userId));
    }

    @Operation(summary = "가족 커스텀 질문 생성", description = "로그인한 사용자가 속한 가족에 커스텀 질문을 추가합니다. 생성 직후 책장 항목 형태(답변=null)로 반환합니다.")
    @PostMapping("/custom-questions")
    public ApiResponse<BookshelfEntryDto> createCustomQuestion(
            @AuthenticationPrincipal CustomUserDetails me,
            @RequestBody @Valid CustomBookshelfQuestionCreateRequestDto req
    ) {
        Long meId = authService.getUserId(me);
        return ApiResponse.onSuccess(bookshelfService.createCustomQuestion(meId, req));
    }

    @Operation(summary = "가족 커스텀 질문 삭제", description = "같은 가족이라면 누구나 해당 가족의 커스텀 질문을 삭제할 수 있습니다.")
    @DeleteMapping("/custom-questions/{questionId}")
    public ApiResponse<Void> deleteCustomQuestion(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long questionId
    ) {
        Long meId = authService.getUserId(me);
        bookshelfService.deleteCustomQuestion(meId, questionId);
        return ApiResponse.onSuccess(null);
    }

    @Operation(
            summary = "내 답변 저장/수정 (일괄)",
            description = "기본 질문과 가족 커스텀 질문을 함께, 한 번의 요청으로 저장/수정합니다. answer가 null/빈문자열이어도 저장됩니다. " +
                    "서버는 questionId/answer만 사용하며, 클라이언트 편의를 위해 각 항목에 questionText를 추가로 보내도 됩니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "bulk-answers (base+custom mixed)",
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

    @PatchMapping("/me")
    public ApiResponse<Void> writeMyAnswers(
            @AuthenticationPrincipal CustomUserDetails me,
            @RequestBody BookshelfAnswersUpsertRequestDto req
    ) {
        Long meId = authService.getUserId(me);
        bookshelfService.writeMyAnswers(meId, req);
        return ApiResponse.onSuccess(null);
    }
}
