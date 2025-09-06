package com.seasonthon.everflow.app.topic.dto;

import com.seasonthon.everflow.app.topic.domain.Topic;
import com.seasonthon.everflow.app.topic.domain.TopicAnswer;
import com.seasonthon.everflow.app.topic.domain.TopicType;

import java.time.LocalDateTime;
import java.util.List;

public class TopicDto {

    // 생성 요청
    public record TopicCreateRequest(String question, LocalDateTime activeFrom,TopicType topicType){}

    // 토픽 응답
    public record TopicResponse(Long id, String question, LocalDateTime activeFrom, LocalDateTime activeUntil) {
        public static TopicResponse of(Topic t) {
            return new TopicResponse(t.getId(), t.getQuestion(), t.getActiveFrom(), t.getActiveUntil());
        }
    }

    // 수정 요청
    public record TopicUpdateRequest(String question) {}

    // 답변 생성/수정 요청
    public record AnswerCreateRequest(String content) {}
    public record AnswerUpdateRequest(String content) {}

    // 답변 응답
    public record AnswerResponse(Long answerId, Long topicId, Long userId, String nickname, String content, LocalDateTime createdAt) {
        public static AnswerResponse of(TopicAnswer a) {
            return new AnswerResponse(
                    a.getId(),
                    a.getTopic().getId(),
                    a.getUser().getId(),
                    a.getUser().getNickname(),
                    a.getContent(),
                    a.getCreatedAt()
            );
        }
    }

    // (옵션) 토픽 + 답변 묶음
    public record TopicWithAnswersResponse(
            Long topicId, String question, LocalDateTime activeFrom, LocalDateTime activeUntil, List<AnswerResponse> answers
    ) {
        public static TopicWithAnswersResponse of(Topic t, List<AnswerResponse> answers) {
            return new TopicWithAnswersResponse(t.getId(), t.getQuestion(), t.getActiveFrom(), t.getActiveUntil(), answers);
        }
    }

    // 가족이 답변한 토픽 목록 & 개수
    public record FamilyAnsweredTopicsResponse(List<TopicResponse> topics, int totalCount) {
        public static FamilyAnsweredTopicsResponse of(List<TopicResponse> topics) {
            return new FamilyAnsweredTopicsResponse(topics, topics.size());
        }
    }
}