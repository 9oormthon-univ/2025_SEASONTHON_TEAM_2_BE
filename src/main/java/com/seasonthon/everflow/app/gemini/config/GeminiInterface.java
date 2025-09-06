package com.seasonthon.everflow.app.gemini.config;

import com.seasonthon.everflow.app.gemini.dto.GeminiResponseDto;
import com.seasonthon.everflow.app.gemini.dto.GeminiRequestDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/v1beta/models/")
public interface GeminiInterface {
    @PostExchange("{model}:generateContent")
    GeminiResponseDto getCompletion(
            @PathVariable String model,
            @RequestBody GeminiRequestDto request
    );
}
