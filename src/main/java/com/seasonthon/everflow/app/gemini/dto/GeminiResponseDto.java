package com.seasonthon.everflow.app.gemini.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@NoArgsConstructor
@Getter
public class GeminiResponseDto {
    private List<Candidate> candidates = Collections.emptyList();

    @Getter
    @NoArgsConstructor
    public static class Candidate {
        private Content content = new Content();
    }

    @Getter
    @NoArgsConstructor
    public static class Content {
        private List<TextPart> parts = Collections.emptyList();
        private String role = "";
    }

    @Getter
    @NoArgsConstructor
    public static class TextPart {
        private String text = "";
    }
}