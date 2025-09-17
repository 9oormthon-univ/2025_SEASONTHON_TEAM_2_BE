package com.seasonthon.everflow.app.topic.dto;

import com.seasonthon.everflow.app.topic.domain.Topic;
import java.time.LocalDateTime;
import java.util.List;

public class TopicResponseDto {

    public record Simple(Long id, String question, LocalDateTime activeFrom, LocalDateTime activeUntil) {
        public static Simple of(Topic t) {
            return new Simple(t.getId(), t.getQuestion(), t.getActiveFrom(), t.getActiveUntil());
        }
    }

    public record WithAnswers(
            Long topicId,
            String question,
            LocalDateTime activeFrom,
            LocalDateTime activeUntil,
            List<TopicAnswerResponseDto.Info> answers
    ) {
        public static WithAnswers of(Topic t, List<TopicAnswerResponseDto.Info> answers) {
            return new WithAnswers(t.getId(), t.getQuestion(), t.getActiveFrom(), t.getActiveUntil(), answers);
        }
    }

    public record FamilyAnswered(List<Simple> topics, int totalCount) {
        public static FamilyAnswered of(List<Simple> topics) {
            return new FamilyAnswered(topics, topics.size());
        }
    }
}
