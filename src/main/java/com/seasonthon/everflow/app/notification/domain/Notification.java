package com.seasonthon.everflow.app.notification.domain;

import com.seasonthon.everflow.app.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    @Column(name = "content_text", nullable = false)
    private String contentText;

    @Enumerated(EnumType.STRING)
    @Column(name = "read_status", nullable = false)
    @Builder.Default // 빌더의 기본값을 UNREAD로 설정
    private ReadStatus readStatus = ReadStatus.UNREAD;

    @Column(name = "link")
    private String link;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 알림을 읽음 상태로 변경하는 메서드
     */
    public void markAsRead() {
        this.readStatus = ReadStatus.READ;
    }
}
