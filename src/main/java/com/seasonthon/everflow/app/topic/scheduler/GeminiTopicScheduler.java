package com.seasonthon.everflow.app.topic.scheduler;

import com.seasonthon.everflow.app.topic.domain.Topic;
import com.seasonthon.everflow.app.topic.domain.TopicType;
import com.seasonthon.everflow.app.topic.repository.TopicRepository;
import com.seasonthon.everflow.app.topic.service.TopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiTopicScheduler {

    private final TopicService topicService;
    private final Random random = new Random();
    private final TopicRepository topicRepository;


    @Scheduled(cron = "* 5 0 * * *", zone = "Asia/Seoul")
    public void createDailyTopic() {
        log.info("Gemini 오늘의 질문 생성을 시작합니다.");
        try {
            TopicType selectedType = selectTopicTypeByWeight();
            log.info("요청할 질문 타입: {}", selectedType);

            List<String> recentQuestions = topicRepository.findTop5ByOrderByIdDesc()
                    .stream()
                    .map(Topic::getQuestion)
                    .toList();

            topicService.createDailyTopicFromGemini(selectedType, recentQuestions);
            log.info("{} 타입의 오늘의 질문 생성이 성공적으로 완료되었습니다.", selectedType);

        } catch (Exception e) {
            log.error("Gemini 오늘의 질문 생성 중 오류 발생", e);
        }
    }


    private TopicType selectTopicTypeByWeight() {
        double pivot = random.nextDouble();

        if (pivot < 0.4) {
            return TopicType.CASUAL;
        } else if (pivot < 0.8) {
            return TopicType.CLOSER;
        } else {
            return TopicType.DEEP;
        }
    }
}