package com.seasonthon.everflow.app.topic.dto;

public class TopicAnswerRequestDto {
    public record Create(String content) {}
    public record Update(String content) {}
}