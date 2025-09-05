package com.seasonthon.everflow.app.topic.controller;

import com.seasonthon.everflow.app.global.code.dto.ApiResponse;
import com.seasonthon.everflow.app.global.oauth.domain.CustomUserDetails;
import com.seasonthon.everflow.app.topic.dto.TopicDto;
import com.seasonthon.everflow.app.topic.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.seasonthon.everflow.app.user.repository.UserRepository;

import java.util.List;

@Tag(name = "Topics API", description = "토픽 생성/수정/조회 및 답변 API")
@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final UserRepository userRepository;
    private final TopicService topicService;

    // 1) 토픽 등록 (관리자)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] 토픽 등록", description = "관리자 전용: 질문과 시작 시각을 받아 3일 활성 토픽을 생성합니다.")
    @PostMapping
    public ApiResponse<TopicDto.TopicResponse> createTopic(@RequestBody TopicDto.TopicCreateRequest req) {
        return ApiResponse.onSuccess(topicService.createTopic(req));
    }

    // 2) 토픽 수정 (관리자)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] 토픽 문구 수정", description = "관리자 전용: 토픽 질문 문구를 수정합니다.")
    @PatchMapping("/{topicId}")
    public ApiResponse<TopicDto.TopicResponse> updateTopic(
            @PathVariable Long topicId,
            @RequestBody TopicDto.TopicUpdateRequest req
    ) {
        return ApiResponse.onSuccess(topicService.updateTopic(topicId, req));
    }

    // 3) 우리 가족이 답변 남긴 토픽 목록 & 개수 (로그인 사용자 가족 기준)
    @Operation(summary = "가족이 답변한 토픽 목록 & 개수", description = "로그인 사용자의 가족이 답변 남긴 모든 토픽 리스트와 총 개수를 반환합니다.")
    @GetMapping("/family/answered")
    public ApiResponse<TopicDto.FamilyAnsweredTopicsResponse> getFamilyAnsweredTopics(
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        if (me == null) return ApiResponse.onFailure("AUTH401","인증 정보가 없습니다.", null);

        Long userId = me.getUserId();
        Long familyId = userRepository.findById(userId)
                .map(u -> u.getFamily() != null ? u.getFamily().getId() : null)
                .orElse(null);
        if (familyId == null) return ApiResponse.onFailure("FAMILY404","가족 정보가 없습니다.", null);

        return ApiResponse.onSuccess(topicService.getFamilyAnsweredTopics(familyId));
    }

    // 4) 특정 토픽에 대한 우리 가족 답변 (로그인 사용자 가족 기준)
    @Operation(summary = "특정 토픽의 우리 가족 답변", description = "해당 토픽에 대해, 로그인 사용자의 가족 구성원 답변(본인 포함)만 조회합니다.")
    @GetMapping("/{topicId}/answers/family")
    public ApiResponse<List<TopicDto.AnswerResponse>> getFamilyAnswersByTopic(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long topicId
    ) {
        if (me == null) return ApiResponse.onFailure("AUTH401","인증 정보가 없습니다.", null);

        Long userId = me.getUserId();
        Long familyId = userRepository.findById(userId)
                .map(u -> u.getFamily() != null ? u.getFamily().getId() : null)
                .orElse(null);
        if (familyId == null) return ApiResponse.onFailure("FAMILY404","가족 정보가 없습니다.", null);

        return ApiResponse.onSuccess(topicService.getFamilyAnswersByTopic(topicId, familyId));
    }

    // (선택) 전체 공개로 특정 토픽의 모든 답변
    @Operation(summary = "특정 토픽의 모든 답변(전체)", description = "가족 제한 없이 해당 토픽의 모든 답변을 조회합니다.")
    @GetMapping("/{topicId}/answers")
    public ApiResponse<List<TopicDto.AnswerResponse>> getTopicAnswers(@PathVariable Long topicId) {
        return ApiResponse.onSuccess(topicService.getTopicAnswers(topicId));
    }

    // 5) 토픽에 답변 (내 것만)
    @Operation(summary = "토픽에 답변 작성(본인)", description = "활성화된 토픽에 한해 로그인 사용자의 답변을 작성합니다. (이미 답변이 있으면 409, 수정은 별도 API 사용)")
    @PostMapping("/{topicId}/answers")
    public ApiResponse<TopicDto.AnswerResponse> createMyAnswer(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long topicId,
            @RequestBody TopicDto.AnswerCreateRequest req
    ) {
        if (me == null) return ApiResponse.onFailure("AUTH401","인증 정보가 없습니다.", null);
        return ApiResponse.onSuccess(topicService.createAnswer(topicId, me.getUserId(), req));
    }

    // 6) 토픽 답변 수정 (내 것만)
    @Operation(summary = "토픽 답변 수정(본인)", description = "내가 작성한 활성 토픽의 답변 내용을 수정합니다.")
    @PatchMapping("/{topicId}/answers")
    public ApiResponse<TopicDto.AnswerResponse> updateMyAnswer(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long topicId,
            @RequestBody TopicDto.AnswerUpdateRequest req
    ) {
        if (me == null) return ApiResponse.onFailure("AUTH401","인증 정보가 없습니다.", null);
        return ApiResponse.onSuccess(topicService.updateAnswer(topicId, me.getUserId(), req));
    }
}