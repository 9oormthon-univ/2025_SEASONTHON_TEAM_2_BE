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
            LocalDateTime createdAt
    ) {
        public static Info of(TopicAnswer a) {
            var user = a.getUser();
            String profileUrl = (user != null) ? user.getProfileUrl() : null;
            return new Info(
                    a.getId(),
                    a.getTopic().getId(),
                    user.getId(),
                    user.getNickname(),
                    profileUrl,
                    a.getContent(),
                    a.getCreatedAt()
            );
        }
    }
}