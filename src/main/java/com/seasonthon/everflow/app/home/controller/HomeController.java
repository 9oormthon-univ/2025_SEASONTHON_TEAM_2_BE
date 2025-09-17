package com.seasonthon.everflow.app.home.controller;

import com.seasonthon.everflow.app.global.code.dto.ApiResponse;
import com.seasonthon.everflow.app.global.oauth.domain.CustomUserDetails;
import com.seasonthon.everflow.app.global.oauth.service.AuthService;
import com.seasonthon.everflow.app.home.dto.ClosenessResponseDto;
import com.seasonthon.everflow.app.home.dto.FamilySummaryResponseDto;
import com.seasonthon.everflow.app.home.service.HomeService;
import com.seasonthon.everflow.app.topic.dto.TopicAnswerResponseDto;
import com.seasonthon.everflow.app.topic.dto.TopicResponseDto;
import com.seasonthon.everflow.app.topic.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
@Tag(name = "Home API", description = "홈 화면 관련 API")
public class HomeController {

    private final HomeService homeService;
    private final TopicService topicService;
    private final AuthService authService;

    @Operation(summary = "가족 친밀도(참여율)", description = "내 누적 답변 수 / 가족 내 최대 답변 수 × 100 (최근 30일 기준)")
    @GetMapping("/progress")
    public ApiResponse<ClosenessResponseDto> getProgress(
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        Long userId = authService.getUserId(me);
        Long familyId = authService.getFamilyId(me);
        ClosenessResponseDto dto = homeService.getCloseness(userId, familyId);
        return ApiResponse.onSuccess(dto);
    }

    @Operation(summary = "현재 활성 토픽 조회", description = "활성 기간에 해당하는 오늘의 질문과 남은 일수(daysLeft)를 반환합니다.")
    @GetMapping("/topics/current")
    public ApiResponse<TopicResponseDto.Simple> getCurrentTopic() {
        return ApiResponse.onSuccess(topicService.getCurrentActiveTopic());
    }

    @Operation(
            summary = "가족 책장 목록",
            description = "같은 가족 구성원의 userId, 닉네임, 책 색을 조회합니다."
    )
    @GetMapping("/bookshelves")
    public ApiResponse<FamilySummaryResponseDto> getFamilySummary(
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        Long meId = authService.getUserId(me);
        return ApiResponse.onSuccess(homeService.getFamilySummary(meId));
    }

    @Operation(summary = "활성 토픽의 우리 가족 답변 조회", description = "로그인 사용자의 가족 기준으로, 활성 토픽에 남긴 모든 답변(본인 포함)을 조회합니다.")
    @GetMapping("/topics/active/answers")
    public ApiResponse<List<TopicAnswerResponseDto.Info>> getActiveTopicFamilyAnswers(
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        Long familyId = authService.getFamilyId(me);
        return ApiResponse.onSuccess(topicService.getFamilyAnswers(familyId));
    }
}
