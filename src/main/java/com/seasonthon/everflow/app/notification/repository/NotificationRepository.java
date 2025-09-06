package com.seasonthon.everflow.app.notification.repository;

import com.seasonthon.everflow.app.notification.domain.Notification;
import com.seasonthon.everflow.app.notification.domain.NotificationType;
import com.seasonthon.everflow.app.notification.domain.ReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByUserIdAndReadStatusOrderByCreatedAtDesc(Long userId, ReadStatus readStatus);
    List<Notification> findTop3ByUserIdAndReadStatusOrderByCreatedAtDesc(Long userId, ReadStatus readStatus);

    // 새롭게 추가된 코드
    Optional<Notification> findByUserIdAndLinkAndNotificationType(Long userId, String link, NotificationType notificationType);

    @Modifying
    @Query("UPDATE Notification n SET n.readStatus = :status " +
            "WHERE n.user.id = :userId AND n.readStatus = 'UNREAD' " +
            "AND n.notificationType NOT IN :excludedTypes")
    int updateAllToReadByUser(
            @Param("userId") Long userId,
            @Param("status") ReadStatus status,
            @Param("excludedTypes") List<NotificationType> excludedTypes
    );
}