package com.seasonthon.everflow.app.notification.service;

import com.seasonthon.everflow.app.global.code.status.ErrorStatus;
import com.seasonthon.everflow.app.global.exception.GeneralException;
import com.seasonthon.everflow.app.notification.domain.Notification;
import com.seasonthon.everflow.app.notification.domain.NotificationType;
import com.seasonthon.everflow.app.notification.domain.ReadStatus;
import com.seasonthon.everflow.app.notification.dto.NotificationResponseDto;
import com.seasonthon.everflow.app.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}
