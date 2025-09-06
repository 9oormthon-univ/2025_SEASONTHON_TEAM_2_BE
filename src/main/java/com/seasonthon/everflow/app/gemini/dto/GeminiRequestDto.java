package com.seasonthon.everflow.app.gemini.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@NoArgsConstructor
@Getter
public class GeminiRequestDto {

    private List<Content> contents;

    public GeminiRequestDto(String text) {
        this.contents = List.of(new Content(Collections.singletonList(new TextPart(text))));
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
}