package com.seasonthon.everflow.app.notification.repository;

import com.seasonthon.everflow.app.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}