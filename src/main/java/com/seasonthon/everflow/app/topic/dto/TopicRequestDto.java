package com.seasonthon.everflow.app.topic.dto;

import com.seasonthon.everflow.app.topic.domain.TopicType;
import java.time.LocalDateTime;

public class TopicRequestDto {

    public record Create(String question, LocalDateTime activeFrom, TopicType topicType) {}

    public record Update(String question) {}
}
