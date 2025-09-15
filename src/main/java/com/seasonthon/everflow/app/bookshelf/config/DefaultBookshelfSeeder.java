package com.seasonthon.everflow.app.bookshelf.config;

import com.seasonthon.everflow.app.bookshelf.domain.BookshelfQuestion;
import com.seasonthon.everflow.app.bookshelf.repository.BookshelfQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DefaultBookshelfSeeder implements CommandLineRunner {

    private final BookshelfQuestionRepository repository;

    private static final List<String> DEFAULTS = List.of(
            "내 이름에 담긴 특별한 의미는 무엇인가요?",
            "내가 태어난 날과 생일 이야기를 들려주세요.",
            "나의 MBTI는 무엇인가요? (16개 유형 중 하나를 선택해주세요)",
            "내가 입는 옷 사이즈와 신발 사이즈를 기록해둬요.",
            "즐겨 부르는 노래방 애창곡(최애 곡)은 무엇인가요?",
            "정말 좋아하는 음식과, 잘 안 맞는 음식은?",
            "나만의 소중한 취미나 즐거운 시간을 보내는 방법은?",
            "마음을 가장 끌어당기는 색깔은 무엇인가요?",
            "사계절 중 가장 좋아하는 계절과 그 이유는?",
            "즐겨보는 드라마·TV 프로그램이나 영화는 무엇인가요?",
            "언젠가 꼭 가보고 싶은 여행지는 어디인가요?",
            "내 인생에서 가장 행복했던 순간은 언제였나요?",
            "내가 생각하는 나의 가장 큰 장점은 무엇인가요?",
            "나의 태몽 또는 태명에 얽힌 이야기가 있나요?",
            "요즘 마음속에 품고 있는, 받고 싶은 선물은 무엇인가요?"
    );

    private static final String MBTI_TEXT =
            "나의 MBTI는 무엇인가요? (16개 유형 중 하나를 선택해주세요)";

    private static final String MBTI_OPTIONS =
            String.join(",",
                    "INTJ","INTP","ENTJ","ENTP",
                    "INFJ","INFP","ENFJ","ENFP",
                    "ISTJ","ISFJ","ESTJ","ESFJ",
                    "ISTP","ISFP","ESTP","ESFP"
            ); // 저장 포맷은 콤마 문자열(프론트에서 split) 또는 JSON 배열로 바꿔도 OK

    @Override
    @Transactional
    public void run(String... args) {
        for (String q : DEFAULTS) {
            repository.findByQuestionText(q).ifPresentOrElse(
                    exist -> {
                        // 존재 시: MBTI라면 옵션 채워주기(비어 있으면)
                        if (q.equals(MBTI_TEXT) && (exist.getOptions() == null || exist.getOptions().isBlank())) {
                            exist.updateOptions(MBTI_OPTIONS);
                        }
                    },
                    () -> {
                        boolean isMbti = q.equals(MBTI_TEXT);
                        repository.save(BookshelfQuestion.base(
                                q,
                                isMbti ? "SELECT" : "TEXT",
                                isMbti ? MBTI_OPTIONS : null
                        ));
                    }
            );
        }
    }
}