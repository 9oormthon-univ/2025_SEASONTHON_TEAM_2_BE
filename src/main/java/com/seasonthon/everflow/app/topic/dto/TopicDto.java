package com.seasonthon.everflow.app.topic.dto;

import com.seasonthon.everflow.app.topic.domain.Topic;
import com.seasonthon.everflow.app.topic.domain.TopicAnswer;
import java.time.LocalDateTime;

public class TopicDto {

    public record TopicCreateRequest(
            String question,
            LocalDateTime activeFrom     // null이면 now 기준으로 계산
    ) {}

    public record TopicResponse(Long id, String question, LocalDateTime activeFrom, LocalDateTime activeUntil) {
        public static TopicResponse of(Topic t) {
            return new TopicResponse(t.getId(), t.getQuestion(), t.getActiveFrom(), t.getActiveUntil());
        }
    }

    public record TopicUpdateRequest(String question) {}

    public record AnswerCreateRequest(String content) {}

    public record AnswerUpdateRequest(String content) {}

    public record AnswerResponse(Long answerId, Long topicId, Long userId, String nickname, String content, LocalDateTime createdAt) {
        public static AnswerResponse of(TopicAnswer a) {
            return new AnswerResponse(a.getId(), a.getTopic().getId(), a.getUser().getId(),
                    a.getUser().getNickname(), a.getContent(), a.getCreatedAt());
        }
    }

    public record TopicWithAnswersResponse(
            Long topicId,
            String question,
            java.time.LocalDateTime activeFrom,
            java.time.LocalDateTime activeUntil,
            java.util.List<AnswerResponse> answers
    ) {
        public static TopicWithAnswersResponse of(com.seasonthon.everflow.app.topic.domain.Topic t,
                                                  java.util.List<AnswerResponse> answers) {
            return new TopicWithAnswersResponse(
                    t.getId(),
                    t.getQuestion(),
                    t.getActiveFrom(),
                    t.getActiveUntil(),
                    answers
            );
        }
    }
}
