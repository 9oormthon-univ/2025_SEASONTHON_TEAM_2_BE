package com.seasonthon.everflow.app.notification.service;

import com.seasonthon.everflow.app.global.code.status.ErrorStatus;
import com.seasonthon.everflow.app.global.exception.GeneralException;
import com.seasonthon.everflow.app.notification.domain.Notification;
import com.seasonthon.everflow.app.notification.domain.NotificationType;
import com.seasonthon.everflow.app.notification.domain.ReadStatus;
import com.seasonthon.everflow.app.notification.dto.NotificationResponseDto;
import com.seasonthon.everflow.app.notification.repository.NotificationRepository;
import com.seasonthon.everflow.app.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

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

    public void sendNotification(User recipient, NotificationType type, String content, String link) {
        Notification notification = Notification.builder()
                .user(recipient)
                .notificationType(type)
                .contentText(content)
                .link(link)
                .build();
        notificationRepository.save(notification);
    }

}
