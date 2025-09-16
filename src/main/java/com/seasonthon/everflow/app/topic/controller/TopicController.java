package com.seasonthon.everflow.app.topic.controller;

import com.seasonthon.everflow.app.global.code.dto.ApiResponse;
import com.seasonthon.everflow.app.global.oauth.domain.CustomUserDetails;
import com.seasonthon.everflow.app.global.oauth.service.AuthService;
import com.seasonthon.everflow.app.topic.domain.Topic;
import com.seasonthon.everflow.app.topic.domain.TopicType;
import com.seasonthon.everflow.app.topic.dto.*; // DTO import를 개별 파일로 수정
import com.seasonthon.everflow.app.topic.repository.TopicRepository;
import com.seasonthon.everflow.app.topic.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Topics API", description = "토픽 생성/수정/조회 및 답변 API")
@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final AuthService authService;
    private final TopicService topicService;
    private final TopicRepository topicRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] 토픽 등록", description = "관리자 전용: 질문과 시작 시각을 받아 3일 활성 토픽을 생성합니다.")
    @PostMapping
    public ApiResponse<TopicResponseDto.Simple> createTopic(@RequestBody TopicRequestDto.Create req) {
        return ApiResponse.onSuccess(topicService.createTopic(req));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Gemini 토픽 생성", description = "관리자 전용: Gemini를 통해 특정 타입의 토픽을 수동 생성합니다.")
    @PostMapping("/admin/daily")
    public ApiResponse<TopicResponseDto.Simple> makeDaily(@RequestParam TopicType type) {
        List<String> recentQuestions = topicRepository.findTop5ByOrderByIdDesc()
                .stream()
                .map(Topic::getQuestion)
                .toList();
        return ApiResponse.onSuccess(topicService.createDailyTopicFromGemini(type, recentQuestions));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] 토픽 문구 수정", description = "관리자 전용: 토픽 질문 문구를 수정합니다.")
    @PatchMapping("/{topicId}")
    public ApiResponse<TopicResponseDto.Simple> updateTopic(
            @PathVariable Long topicId,
            @RequestBody TopicRequestDto.Update req
    ) {
        return ApiResponse.onSuccess(topicService.updateTopic(topicId, req));
    }

    @Operation(summary = "가족이 답변한 토픽 목록 & 개수", description = "로그인 사용자의 가족이 답변 남긴 모든 토픽 리스트와 총 개수를 반환합니다.")
    @GetMapping("/family/answered")
    public ApiResponse<TopicResponseDto.FamilyAnswered> getFamilyAnsweredTopics(
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        Long familyId = authService.getFamilyId(me);
        return ApiResponse.onSuccess(topicService.getFamilyAnsweredTopics(familyId));
    }

    @Operation(summary = "특정 토픽의 우리 가족 답변", description = "해당 토픽에 대해, 로그인 사용자의 가족 구성원 답변(본인 포함)만 조회합니다.")
    @GetMapping("/{topicId}/answers/family")
    public ApiResponse<List<TopicAnswerResponseDto.Info>> getFamilyAnswersByTopic(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long topicId
    ) {
        Long familyId = authService.getFamilyId(me);
        return ApiResponse.onSuccess(topicService.getFamilyAnswersByTopic(topicId, familyId));
    }

    @Operation(summary = "특정 토픽의 모든 답변(전체)", description = "가족 제한 없이 해당 토픽의 모든 답변을 조회합니다.")
    @GetMapping("/{topicId}/answers")
    public ApiResponse<List<TopicAnswerResponseDto.Info>> getTopicAnswers(@PathVariable Long topicId) {
        return ApiResponse.onSuccess(topicService.getTopicAnswers(topicId));
    }

    @Operation(summary = "토픽에 답변 작성(본인)", description = "활성화된 토픽에 한해 로그인 사용자의 답변을 작성합니다. (이미 답변이 있으면 409, 수정은 별도 API 사용)")
    @PostMapping("/{topicId}/answers")
    public ApiResponse<TopicAnswerResponseDto.Info> createMyAnswer(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long topicId,
            @RequestBody TopicAnswerRequestDto.Create req
    ) {
        Long userId = authService.getUserId(me);
        return ApiResponse.onSuccess(topicService.createAnswer(topicId, userId, req));
    }

    @Operation(summary = "토픽 답변 수정(본인)", description = "내가 작성한 활성 토픽의 답변 내용을 수정합니다.")
    @PatchMapping("/{topicId}/answers")
    public ApiResponse<TopicAnswerResponseDto.Info> updateMyAnswer(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long topicId,
            @RequestBody TopicAnswerRequestDto.Update req
    ) {
        Long userId = authService.getUserId(me);
        return ApiResponse.onSuccess(topicService.updateAnswer(topicId, userId, req));
    }
}