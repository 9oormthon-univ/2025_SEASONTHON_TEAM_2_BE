package com.seasonthon.everflow.app.topic.dto;

import com.seasonthon.everflow.app.topic.domain.TopicAnswer;
import java.time.LocalDateTime;

public class TopicAnswerResponseDto {

    public record Info(
            Long answerId,
            Long topicId,
            Long userId,
            String nickname,
            String profileUrl,
            String content,
            LocalDateTime respondedAt
    ) {
        public static Info of(TopicAnswer a) {
            var user = a.getUser();
            String profileUrl = (user != null) ? user.getProfileUrl() : null;

            LocalDateTime timestamp = (a.getUpdatedAt() != null && a.getUpdatedAt().isAfter(a.getCreatedAt()))
                    ? a.getUpdatedAt()
                    : a.getCreatedAt();

            return new Info(
                    a.getId(),
                    a.getTopic().getId(),
                    user.getId(),
                    user.getNickname(),
                    profileUrl,
                    a.getContent(),
                    timestamp
            );
        }
    }
}