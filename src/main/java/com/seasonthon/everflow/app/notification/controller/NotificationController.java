package com.seasonthon.everflow.app.notification.controller;

import com.seasonthon.everflow.app.global.code.dto.ApiResponse;
import com.seasonthon.everflow.app.global.code.status.SuccessStatus;
import com.seasonthon.everflow.app.global.oauth.domain.CustomUserDetails;
import com.seasonthon.everflow.app.notification.dto.NotificationResponseDto;
import com.seasonthon.everflow.app.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jdk.jfr.Description;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name="Notification API", description = "알림 관련 api")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "알림 읽기", description = "알림상태를 읽기로 전환합니다.")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationResponseDto.ReadResponseDto>> readNotification(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long currentUserId = userDetails.getUserId();

        NotificationResponseDto.ReadResponseDto resultDto =
                notificationService.readNotification(notificationId, currentUserId);

        return ResponseEntity.ok(ApiResponse.of(SuccessStatus.OK, resultDto));
    }

    @Operation(summary = "알림 모두 읽기", description = "액션 알림을 제외한 모든 알림상태를 읽기로 전환합니다.")
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<NotificationResponseDto.ReadResponseDto>> readAllNotification(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long currentUserId = userDetails.getUserId();

        NotificationResponseDto.ReadResponseDto resultDto =
                notificationService.readALLNotification(currentUserId);

        return ResponseEntity.ok(ApiResponse.of(SuccessStatus.OK, resultDto));
    }

    @Operation(summary = "내 모든 알림 조회", description = "읽지 않은 모든 알림을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponseDto.NotificationGetResponseDto>>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long currentUserId = userDetails.getUserId();
        List<NotificationResponseDto.NotificationGetResponseDto> resultDtoList =
                notificationService.getNotifications(currentUserId);

        return ResponseEntity.ok(ApiResponse.of(SuccessStatus.OK, resultDtoList));
    }

    @Operation(summary = "내 최근 알림 3개 조회", description = "읽지 않은 최근 알림 3개를 조회합니다.")
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<NotificationResponseDto.NotificationGetResponseDto>>> getRecentNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long currentUserId = userDetails.getUserId();
        List<NotificationResponseDto.NotificationGetResponseDto> resultDtoList =
                notificationService.getRecentNotifications(currentUserId);

        return ResponseEntity.ok(ApiResponse.of(SuccessStatus.OK, resultDtoList));
    }
}
