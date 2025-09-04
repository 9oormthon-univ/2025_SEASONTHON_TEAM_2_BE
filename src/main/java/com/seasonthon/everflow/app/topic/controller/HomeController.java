package com.seasonthon.everflow.app.topic.controller;

import com.seasonthon.everflow.app.global.code.dto.ApiResponse;
import com.seasonthon.everflow.app.topic.dto.TopicDto.TopicResponse;
import com.seasonthon.everflow.app.topic.service.TopicService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
@Tag(name = "Home API", description = "홈 화면 관련 API (현재 활성 토픽 조회)")
public class HomeController {

    private final TopicService topicService;

    /** GET /api/home/topics/current : 활성화된 토픽 조회 */
    @Operation(summary = "현재 활성 토픽 조회", description = "활성 기간에 해당하는 오늘의 질문을 반환합니다.")
    @GetMapping("/topics/current")
    public ApiResponse<TopicResponse> getCurrent() {
        TopicResponse dto = topicService.getCurrentActiveTopic();
        return ApiResponse.onSuccess(dto);
    }
}