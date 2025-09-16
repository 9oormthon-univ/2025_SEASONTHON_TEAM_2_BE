package com.seasonthon.everflow.app.gemini.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@NoArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeminiRequestDto {

    private List<Content> contents;
    private GenerationConfig generationConfig;

    public GeminiRequestDto(String text, GenerationConfig config) {
        this.contents = List.of(new Content(Collections.singletonList(new TextPart(text))));
        this.generationConfig = config;
    }

    @Getter
    @AllArgsConstructor
    private static class Content {
        private List<Part> parts;
    }

    interface Part {}

    @Getter
    @AllArgsConstructor
    private static class TextPart implements Part {
        private String text;
    }

    @Getter
    @AllArgsConstructor
    public static class GenerationConfig {
        private float temperature;
    }
}