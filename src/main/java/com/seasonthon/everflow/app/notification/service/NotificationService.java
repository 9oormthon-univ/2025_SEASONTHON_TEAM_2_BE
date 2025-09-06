package com.seasonthon.everflow.app.notification.service;

import com.seasonthon.everflow.app.global.code.status.ErrorStatus;
import com.seasonthon.everflow.app.global.exception.GeneralException;
import com.seasonthon.everflow.app.notification.domain.Notification;
import com.seasonthon.everflow.app.notification.domain.NotificationType;
import com.seasonthon.everflow.app.notification.domain.ReadStatus;
import com.seasonthon.everflow.app.notification.dto.NotificationResponseDto;
import com.seasonthon.everflow.app.notification.repository.EmitterRepository;
import com.seasonthon.everflow.app.notification.repository.NotificationRepository;
import com.seasonthon.everflow.app.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    private final NotificationRepository notificationRepository;
    private final EmitterRepository emitterRepository;

    public SseEmitter subscribe(Long userId) {
        String emitterId = userId + "_" + System.currentTimeMillis();
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(emitterId, emitter);

        emitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
        emitter.onTimeout(() -> emitterRepository.deleteById(emitterId));

        // 503 에러 방지를 위한 더미 이벤트 전송
        sendToClient(emitter, emitterId, "EventStream Created. [userId=" + userId + "]");

        return emitter;
    }

    public void sendNotification(User recipient, NotificationType type, String content, String link) {
        Notification notification = Notification.builder()
                .user(recipient)
                .notificationType(type)
                .contentText(content)
                .link(link)
                .build();
        notificationRepository.save(notification);

        Map<String, SseEmitter> emitters = emitterRepository.findAllByUserId(String.valueOf(recipient.getId()));
        emitters.forEach((emitterId, emitter) -> {
            NotificationResponseDto.NotificationGetResponseDto responseDto = mapToNotificationGetResponseDto(notification);
            sendToClient(emitter, emitterId, responseDto);
        });
    }

    private void sendToClient(SseEmitter emitter, String emitterId, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(emitterId)
                    .name("sse")
                    .data(data));
        } catch (IOException e) {
            emitterRepository.deleteById(emitterId);
            log.error("SSE 연결 오류!", e);
        }
    }

    @Transactional
    public NotificationResponseDto.ReadResponseDto readNotification(Long notificationId, Long userId) {
        // 1. notificationId로 알림을 찾습니다. 없으면 예외를 발생시킵니다.
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOTIFICATION_NOT_FOUND));

        // 2. 권한 확인
        if (!notification.getUser().getId().equals(userId)) {
            throw new GeneralException(ErrorStatus.FORBIDDEN);
        }

        // 3. 엔터티의 상태 변경 메서드를 호출
        notification.markAsRead();

        return new NotificationResponseDto.ReadResponseDto("알림을 읽음 처리했습니다.");
    }

    @Transactional
    public NotificationResponseDto.ReadResponseDto readALLNotification(Long userId) {
        // 액션알림 목록 정의
        List<NotificationType> excludedActionTypes = List.of(
                NotificationType.APPOINTMENT_ACTION,
                NotificationType.FAMILY_ACTION
        );

        // 레포지토리의 bulk update 메서드 호출
        notificationRepository.updateAllToReadByUser(
                userId,
                ReadStatus.READ,
                excludedActionTypes
        );

        return new NotificationResponseDto.ReadResponseDto("모든 알림을 읽음 처리했습니다.");
    }

    public List<NotificationResponseDto.NotificationGetResponseDto> getNotifications(Long userId) {
        // 새로 추가한 메서드를 사용하여 UNREAD 상태의 알림만 조회
        List<Notification> notifications = notificationRepository.findAllByUserIdAndReadStatusOrderByCreatedAtDesc(userId, ReadStatus.UNREAD);

        return notifications.stream()
                .map(this::mapToNotificationGetResponseDto)
                .collect(Collectors.toList());
    }

    public List<NotificationResponseDto.NotificationGetResponseDto> getRecentNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findTop3ByUserIdAndReadStatusOrderByCreatedAtDesc(userId, ReadStatus.UNREAD);

        return notifications.stream()
                .map(this::mapToNotificationGetResponseDto)
                .collect(Collectors.toList());
    }

    private NotificationResponseDto.NotificationGetResponseDto mapToNotificationGetResponseDto(Notification notification) {
        String typeString = convertNotificationTypeToString(notification.getNotificationType());

        return new NotificationResponseDto.NotificationGetResponseDto(
                notification.getId(),
                typeString,
                notification.getContentText(),
                notification.getLink()
        );
    }

    private String convertNotificationTypeToString(NotificationType type) {
        return switch (type) {
            case APPOINTMENT_ACTION, APPOINTMENT_RESPONSE -> "약속알림";
            case FAMILY_ACTION, FAMILY_RESPONSE -> "구성원알림";
            case ANSWER_RESPONSE -> "오늘의 질문 알림";
            default -> "기타알림";
        };
    }

}
