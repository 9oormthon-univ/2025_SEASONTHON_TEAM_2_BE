package com.seasonthon.everflow.app.topic.controller;

import com.seasonthon.everflow.app.global.code.dto.ApiResponse;
import com.seasonthon.everflow.app.topic.dto.TopicDto;
import com.seasonthon.everflow.app.topic.dto.TopicDto.AnswerCreateRequest;
import com.seasonthon.everflow.app.topic.dto.TopicDto.AnswerResponse;
import com.seasonthon.everflow.app.topic.dto.TopicDto.AnswerUpdateRequest;
import com.seasonthon.everflow.app.topic.dto.TopicDto.TopicCreateRequest;
import com.seasonthon.everflow.app.topic.dto.TopicDto.TopicResponse;
import com.seasonthon.everflow.app.topic.dto.TopicDto.TopicUpdateRequest;
import com.seasonthon.everflow.app.topic.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Topic API", description = "오늘의 질문 및 답변 관리 API")
public class TopicController {

    private final TopicService topicService;

    @Operation(summary = "토픽 등록", description = "3일간 활성화될 '오늘의 질문'을 생성합니다.")
    @PostMapping("/topics")
    public ApiResponse<TopicResponse> createTopic(@RequestBody TopicCreateRequest req) {
        TopicResponse dto = topicService.createTopic(req);
        return ApiResponse.onSuccess(dto);
    }

    @Operation(summary = "토픽 수정", description = "질문 문구 등 토픽 정보를 수정합니다.")
    @PatchMapping("/topics")
    public ApiResponse<TopicResponse> updateTopic(@RequestParam Long topicId,
                                                  @RequestBody TopicUpdateRequest req) {
        TopicResponse dto = topicService.updateTopic(topicId, req);
        return ApiResponse.onSuccess(dto);
    }

    @Operation(summary = "토픽에 답변", description = "지정된 토픽에 대한 내 답변을 등록합니다. (한 토픽당 1인 1답변)")
    @PostMapping("/topics/{topicId}/answers")
    public ApiResponse<AnswerResponse> createAnswer(@PathVariable Long topicId,
                                                    @RequestParam Long userId, // 실제 운영에선 SecurityContext 사용 권장
                                                    @RequestBody AnswerCreateRequest req) {
        AnswerResponse dto = topicService.createAnswer(topicId, userId, req);
        return ApiResponse.onSuccess(dto);
    }

    @Operation(summary = "토픽 답변 수정", description = "내가 작성한 답변 내용을 수정합니다.")
    @PatchMapping("/topics/{topicId}/answers")
    public ApiResponse<AnswerResponse> updateAnswer(@PathVariable Long topicId,
                                                    @RequestParam Long userId,
                                                    @RequestBody AnswerUpdateRequest req) {
        AnswerResponse dto = topicService.updateAnswer(topicId, userId, req);
        return ApiResponse.onSuccess(dto);
    }

    @Operation(summary = "특정 토픽 답변 상세 조회", description = "해당 토픽의 모든 답변(본인 포함)을 조회합니다.")
    @GetMapping("/topics/{topicId}/answers")
    public ApiResponse<java.util.List<AnswerResponse>> getTopicAnswers(@PathVariable Long topicId) {
        List<AnswerResponse> list = topicService.getTopicAnswers(topicId);
        return ApiResponse.onSuccess(list);
    }

    @Operation(summary = "가족 답변 목록 조회", description = "현재 활성 토픽에 대해 가족 구성원들이 작성한 답변 목록을 조회합니다.")
    @GetMapping("/topics/{familyId}/answers")
    public ApiResponse<java.util.List<AnswerResponse>> getFamilyAnswers(@PathVariable Long familyId) {
        List<AnswerResponse> list = topicService.getFamilyAnswers(familyId);
        return ApiResponse.onSuccess(list);
    }

    @Operation(summary = "가족의 모든 토픽 답변 묶음 조회", description = "가족 구성원이 과거에 작성한 답변을 토픽별로 묶어 반환합니다.")
    @GetMapping("/topics/answers/family/{familyId}")
    public ApiResponse<List<TopicDto.TopicWithAnswersResponse>> getAllFamilyAnswersGroupedByTopic(
            @PathVariable Long familyId
    ) {
        List<TopicDto.TopicWithAnswersResponse> list = topicService.getFamilyAnswersByTopic(familyId);
        return ApiResponse.onSuccess(list);
    }
}
