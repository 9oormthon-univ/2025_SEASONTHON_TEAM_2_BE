package com.seasonthon.everflow.app.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 1시간

    private final NotificationRepository notificationRepository;
    private final EmitterRepository emitterRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SseEmitter subscribe(Long userId) {
        String emitterId = userId + "_" + System.currentTimeMillis();
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(emitterId, emitter);

        emitter.onCompletion(() -> {
            log.info("SSE onCompletion callback for emitterId: {}", emitterId);
            emitterRepository.deleteById(emitterId);
        });
        emitter.onTimeout(() -> {
            log.info("SSE onTimeout callback for emitterId: {}", emitterId);
            emitterRepository.deleteById(emitterId);
        });

        // 503 에러 방지를 위한 더미 이벤트 전송
        log.info("Sending initial dummy event for emitterId: {}", emitterId);
        sendToClient(emitter, emitterId, "EventStream Created. [userId=" + userId + "]");

        return emitter;
    }

    @Transactional
    public void sendNotification(User recipient, NotificationType type, String content, String link) {
        Notification notification = Notification.builder()
                .user(recipient)
                .notificationType(type)
                .contentText(content)
                .link(link)
                .build();
        notificationRepository.save(notification);

        log.info("Notification saved for user {}. Content: {}", recipient.getId(), content);

        Map<String, SseEmitter> emitters = emitterRepository.findAllByUserId(String.valueOf(recipient.getId()));
        log.info("Found {} emitters for user {}", emitters.size(), recipient.getId());

        emitters.forEach((emitterId, emitter) -> {
            NotificationResponseDto.NotificationGetResponseDto responseDto = mapToNotificationGetResponseDto(notification);
            log.info("Sending notification to emitterId: {}", emitterId);
            sendToClient(emitter, emitterId, responseDto);
        });
    }

    private void sendToClient(SseEmitter emitter, String emitterId, Object data) {
        try {
            String jsonData;
            if (data instanceof String) {
                jsonData = (String) data;
            } else {
                jsonData = objectMapper.writeValueAsString(data);
            }

            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .id(emitterId)
                    .name("sse")
                    .data(jsonData); // 데이터를 JSON 문자열로 전송
            emitter.send(event);
            log.info("Successfully sent data to emitterId: {}: {}", emitterId, jsonData);

        } catch (IOException e) {
            emitterRepository.deleteById(emitterId);
            log.error("SSE connection error for emitterId: {}! - {}", emitterId, e.getMessage());
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