package com.seasonthon.everflow.app.gemini.service;

import com.seasonthon.everflow.app.gemini.config.GeminiInterface;
import com.seasonthon.everflow.app.gemini.dto.GeminiRequestDto;
import com.seasonthon.everflow.app.gemini.dto.GeminiResponseDto;
import com.seasonthon.everflow.app.topic.domain.TopicType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GeminiService {

    private final GeminiInterface geminiInterface;

    @Value("${gemini.model:gemini-2.0-flash}")
    private String defaultModel;

    /**
     * (고수준) 토픽 타입에 맞춰 오늘의 질문 1개 생성
     * - CASUAL(레벨1), CLOSER(레벨2), DEEP(레벨3)
     * - 출력: 순수 질문 문장 1개 (따옴표/번호/불릿 없이)
     */
    public String generateDailyQuestion(TopicType type) {
        String prompt = buildQuestionPrompt(type);
        String raw = getCompletion(prompt, defaultModel);
        return sanitizeQuestion(raw);
    }

    /**
     * (저수준) 프롬프트 텍스트를 그대로 모델에 보내 결과 텍스트를 받아온다.
     */
    public String getCompletion(String prompt, String model) {
        GeminiRequestDto request = new GeminiRequestDto(prompt);
        GeminiResponseDto response = geminiInterface.getCompletion(model, request);

        if (response == null || response.getCandidates() == null || response.getCandidates().isEmpty()) {
            throw new IllegalStateException("Gemini 응답이 비어있음(candidates null/empty)");
        }

        return response.getCandidates().stream()
                .findFirst()
                .flatMap(c -> Optional.ofNullable(c.getContent())
                        .filter(cnt -> cnt.getParts() != null && !cnt.getParts().isEmpty())
                        .flatMap(cnt -> cnt.getParts().stream().findFirst())
                        .map(GeminiResponseDto.TextPart::getText))
                .orElseThrow(() -> new IllegalStateException("Gemini 응답 내용 파싱 실패 (parts null/empty)"));
    }

    /**
     * 모델 응답을 한 줄의 깔끔한 질문으로 정제:
     * - 첫 줄만 사용
     * - 양쪽 따옴표/백틱 제거
     * - 불릿/번호/접두어(Level …, 레벨 …, Q:, 1. 등) 제거
     * - 45자 초과 시 공백 기준으로 자르고 ? 보장
     */
    private String sanitizeQuestion(String raw) {
        if (raw == null) return "";

        // 첫 줄만
        String line = raw.strip().split("\\R", 2)[0];

        // 주변 따옴표/백틱 제거
        if ((line.startsWith("\"") && line.endsWith("\"")) ||
                (line.startsWith("“") && line.endsWith("”")) ||
                (line.startsWith("'") && line.endsWith("'")) ||
                (line.startsWith("`") && line.endsWith("`"))) {
            line = line.substring(1, line.length() - 1);
        }

        // 불릿/번호/접두어 제거
        line = line
                .replaceFirst("^[•\\-*\\u2022\\u2043\\u2219]\\s*", "")
                .replaceFirst("^\\d+\\s*[\\).:]\\s*", "")
                .replaceFirst("^(?i)Q\\s*[:\\-]\\s*", "")
                .replaceFirst("^\\(?.?Level\\s*\\d+\\)?\\s*[:\\-]\\s*", "")
                .replaceFirst("^레벨\\s*\\d+\\s*[:,\\-]\\s*", "")
                .replaceFirst("^\\(?.?TopicType\\s*[:\\-].*?\\)\\s*", "")
                .replaceFirst("^\\([^)]*\\)\\s*", "")
                .strip();

        // 길이 제한: 최대 45자. 단어 중간이 아닌 공백 기준으로 잘라냄.
        final int MAX = 45;
        if (line.length() > MAX) {
            int cut = line.lastIndexOf(' ', MAX - 1);
            if (cut >= 20) {
                line = line.substring(0, cut).strip();
            } else {
                line = line.substring(0, MAX).strip();
            }
        }

        // 끝이 물음표가 아니면 ? 보장 (마침표 제거 후)
        if (!line.endsWith("?")) {
            line = line.replaceAll("[.]+$", "").strip() + "?";
        }

        return line;
    }

    /**
     * 토픽 타입을 레벨로 매핑하여 프롬프트 구성
     * - %% 이스케이프(80%%/20%%) 적용됨 (String#formatted 사용)
     */
    private String buildQuestionPrompt(TopicType type) {
        int level = switch (type) {
            case CASUAL -> 1;
            case CLOSER -> 2;
            case DEEP -> 3;
        };

        return """
            당신은 한국의 가족 구성원(자녀, 부모, 조부모, 친척)들이 서로 따뜻하게 대화할 수 있도록 돕는 '가족 대화 도우미'입니다.
            당신의 임무는 아래 규칙에 따라 '질문 1개'를 생성하는 것입니다.
            
            [핵심 목표]
            - 세대 누구나 답할 수 있지만, 답변 내용에서 세대 차이가 자연스럽게 드러나야 합니다.
              * (예: '기술' 질문은 디지털 활용도 차이, '추억' 질문은 시대 경험 차이, '가치관' 질문은 삶의 우선순위 차이)
            
            [주제 분포 규칙]
            1) 전체 질문의 약 80%%는 반드시 [가치관], [기술/디지털], [추억] 중 하나에서 생성하세요. (우선 선택)
            2) 나머지 20%% 이내에서만 [생활습관], [음식], [영화], [뉴스], [트렌드]를 사용할 수 있습니다.
            3) [트렌드]를 선택한 경우에만 특정 유행어나 표현을 질문에 직접 포함할 수 있습니다.
               - 금지: '밈/신조어/유행어'라는 단어 자체를 질문에 쓰지 마세요.
               - 예시: "‘궁전으로 갈수도 있어’ 하면 생각나는 건?" / "‘알잘딱깔센’이 무슨 뜻일까요?"
               - 트렌드를 선택하지 않았다면 밈/유행어를 언급하지 마세요.
            
            [어투/형식]
            - 어투: 다정하고 감성적 높임말로 쓰인 완전한 문장
            - 금지 : 이모지 사용과 '혹시'로 시작, 비슷한 문장 반복
            - 길이: 질문 텍스트는 반드시 45자 이내
            - 출력 형식(정확히 준수):
              (Level %d / TopicType: [주제 카테고리]) [질문 텍스트]
            
            [레벨 정의]
            * Level 1: 가벼운 주제 (주로 기술/디지털, 생활습관)
            * Level 2: 개인적인 경험·추억·가치관·특정 트렌드
            * Level 3: 깊은 가치관, 가족 관계, 속마음
            
            [TopicType 카테고리 목록]
            * 기술/디지털
            * 생활습관
            * 가치관
            * 트렌드
            * 음식
            * 영화
            * 뉴스
            * 추억
            
            위의 모든 규칙을 엄격히 지켜 질문 1개만 생성하세요.
            """.formatted(level);
    }
}