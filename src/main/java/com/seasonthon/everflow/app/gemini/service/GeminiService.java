package com.seasonthon.everflow.app.gemini.service;

import com.seasonthon.everflow.app.gemini.config.GeminiInterface;
import com.seasonthon.everflow.app.gemini.dto.GeminiRequestDto;
import com.seasonthon.everflow.app.gemini.dto.GeminiResponseDto;
import com.seasonthon.everflow.app.global.code.status.ErrorStatus;
import com.seasonthon.everflow.app.global.exception.GeneralException;
import com.seasonthon.everflow.app.topic.domain.TopicType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GeminiService {

    private final GeminiInterface geminiInterface;

    @Value("${gemini.model:gemini-1.5-flash-001}")
    private String defaultModel;

    public String generateDailyQuestion(TopicType type, List<String> recentQuestions) {
        String prompt = buildQuestionPrompt(type, recentQuestions);
        String raw = getCompletion(prompt, defaultModel);
        return sanitizeQuestion(raw);
    }

    public String getCompletion(String prompt, String model) {
        GeminiRequestDto.GenerationConfig config = new GeminiRequestDto.GenerationConfig(0.9f);
        GeminiRequestDto request = new GeminiRequestDto(prompt, config);
        final GeminiResponseDto response;
        try {
            response = geminiInterface.getCompletion(model, request);
        } catch (ResourceAccessException e) {
            throw new GeneralException(ErrorStatus.GEMINI_TIMEOUT);
        } catch (HttpStatusCodeException e) {
            throw new GeneralException(ErrorStatus.GEMINI_HTTP_ERROR);
        } catch (RestClientException e) {
            throw new GeneralException(ErrorStatus.GEMINI_CALL_FAILED);
        } catch (RuntimeException e) {
            throw new GeneralException(ErrorStatus.GEMINI_CALL_FAILED);
        }

        if (response == null || response.getCandidates() == null || response.getCandidates().isEmpty()) {
            throw new GeneralException(ErrorStatus.GEMINI_BAD_RESPONSE);
        }

        return response.getCandidates().stream()
                .findFirst()
                .flatMap(c -> Optional.ofNullable(c.getContent())
                        .filter(cnt -> cnt.getParts() != null && !cnt.getParts().isEmpty())
                        .flatMap(cnt -> cnt.getParts().stream().findFirst())
                        .map(GeminiResponseDto.TextPart::getText))
                .orElseThrow(() -> new GeneralException(ErrorStatus.GEMINI_BAD_RESPONSE));
    }

    private String sanitizeQuestion(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new GeneralException(ErrorStatus.GEMINI_EMPTY_QUESTION);
        }
        String cleaned = raw.strip();
        if ((cleaned.startsWith("\"") && cleaned.endsWith("\"")) ||
                (cleaned.startsWith("“") && cleaned.endsWith("”"))) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        if (!cleaned.endsWith("?")) {
            cleaned = cleaned.replaceAll("[.]$", "").strip() + "?";
        }
        if (cleaned.length() > 50) {
            cleaned = cleaned.substring(0, 50).strip() + "?";
        }
        return cleaned;
    }

    private String buildQuestionPrompt(TopicType type, List<String> recentQuestions) {
        String levelDescription = switch (type) {
            case CASUAL -> "가볍고 일상적인 주제(취향, 습관, 유행 등)의 질문 (Level 1)";
            case CLOSER -> "개인적인 경험이나 감정(추억, 고마움, 행복)을 공유하며 유대감을 높이는 질문 (Level 2)";
            case DEEP -> "서로의 가치관, 삶의 우선순위, 중요한 결정 등 깊은 생각을 나누는 질문 (Level 3)";
        };

        String recentQuestionsString = recentQuestions.isEmpty()
                ? "없음"
                : recentQuestions.stream()
                .map(q -> "- " + q)
                .collect(Collectors.joining("\n"));

        return """
            당신은 세대 간의 소통을 돕는 '가족 대화 도우미' AI입니다.
            당신의 임무는 아래 조건에 맞는 따뜻하고 의미 있는 '대화 시작 질문'을 생성하는 것입니다.
    
            [질문 조건]
            1. 주제 및 깊이: "%s"의 성격에 맞춰 질문을 만들어주세요.
            2. 대상: 모든 세대가 답변할 수 있어야 합니다.
            3. 목표: 답변을 통해 세대 간의 경험, 생각, 가치관 차이가 자연스럽게 드러나도록 유도해야 합니다.
            4. 형식: 다정하고 부드러운 높임말을 사용하고, 길이는 45자 이내로 간결해야 합니다.
            
            [최근 생성한 질문 목록 (절대 반복하거나 비슷한 주제를 다루지 마세요)]
            %s 

            [출력 규칙]
            * 절대로 설명, 접두사, 번호, 따옴표, 불릿 등을 붙이지 마세요.
            * 최종 답변은 반드시 정제된 '질문 문장' 하나여야 합니다.
            
            질문 예시 (참고만 하고, 이 예시들은 절대 사용하지 마세요):
            - 최근에 가장 크게 웃었던 적이 언제인가요?
            - 9시 출근이면, 몇 시까지 도착해야 한다고 생각하시나요?
            - 요즘은 다 이렇게 입어요. 새깅 패션에 대해 어떻게 생각하세요?
            - 최근 우리 가족에게 힘이 되어준 노래나 영화, 혹시 있으신가요?
            - 키오스크에서 제일 불편한 점이 뭔가요?
            """.formatted(levelDescription, recentQuestionsString);
    }
}