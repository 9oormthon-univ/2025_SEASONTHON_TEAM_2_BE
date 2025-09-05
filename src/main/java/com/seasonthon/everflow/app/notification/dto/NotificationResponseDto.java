package com.seasonthon.everflow.app.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class NotificationResponseDto {

    @Getter
    @AllArgsConstructor
    public static class ReadResponseDto {
        private String message;
    }

    @Getter
    @AllArgsConstructor
    public static class NotificationGetResponseDto {
        private Long notificationId;
        private String notificationType;
        private String contentText;
        private String link;

    }

}