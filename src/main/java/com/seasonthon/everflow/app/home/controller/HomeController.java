package com.seasonthon.everflow.app.home.controller;

import com.seasonthon.everflow.app.global.code.dto.ApiResponse;
import com.seasonthon.everflow.app.global.oauth.domain.CustomUserDetails;
import com.seasonthon.everflow.app.home.dto.HomeDto;
import com.seasonthon.everflow.app.home.service.HomeService;
import com.seasonthon.everflow.app.topic.dto.TopicDto;
import com.seasonthon.everflow.app.topic.service.TopicService;
import com.seasonthon.everflow.app.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
@Tag(name = "Home API", description = "홈 화면 관련 API")
public class HomeController {

    private final HomeService homeService;
    private final TopicService topicService;
    private final UserRepository userRepository;

    /** 1) 가족 친밀도(참여율) */
    @Operation(summary = "가족 친밀도(참여율)", description = "내 누적 답변 수 / 가족 내 최대 답변 수 × 100 (scope: ALL|ACTIVE|LAST_30D)")
    @GetMapping("/progress")
    public ApiResponse<HomeDto.ClosenessResponse> getProgress(
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        if (me == null) return ApiResponse.onFailure("AUTH401","인증 정보가 없습니다.", null);
        Long userId = me.getUserId();
        Long familyId = userRepository.findById(userId)
                .map(u -> u.getFamily() != null ? u.getFamily().getId() : null)
                .orElse(null);
        if (familyId == null) return ApiResponse.onFailure("FAMILY404","가족 정보가 없습니다.", null);
        HomeDto.ClosenessResponse dto = homeService.getCloseness(userId, familyId);
        return ApiResponse.onSuccess(dto);
    }

    /** 2) 현재 활성 토픽 조회 */
    @Operation(summary = "현재 활성 토픽 조회", description = "활성 기간에 해당하는 오늘의 질문을 반환합니다.")
    @GetMapping("/topics/current")
    public ApiResponse<TopicDto.TopicResponse> getCurrentTopic() {
        return ApiResponse.onSuccess(topicService.getCurrentActiveTopic());
    }

    /** 3) 활성 토픽의 우리 가족 답변 조회(본인 포함) */
    @Operation(summary = "활성 토픽의 우리 가족 답변 조회", description = "로그인 사용자의 가족 기준으로, 활성 토픽에 남긴 모든 답변(본인 포함)을 조회합니다.")
    @GetMapping("/topics/active/answers")
    public ApiResponse<List<TopicDto.AnswerResponse>> getActiveTopicFamilyAnswers(
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        if (me == null) return ApiResponse.onFailure("AUTH401","인증 정보가 없습니다.", null);
        Long userId = me.getUserId();
        Long familyId = userRepository.findById(userId)
                .map(u -> u.getFamily() != null ? u.getFamily().getId() : null)
                .orElse(null);
        if (familyId == null) return ApiResponse.onFailure("FAMILY404","가족 정보가 없습니다.", null);
        return ApiResponse.onSuccess(topicService.getFamilyAnswers(familyId));
    }
}