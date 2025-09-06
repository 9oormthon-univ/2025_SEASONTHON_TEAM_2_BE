package com.seasonthon.everflow.app.topic.scheduler;

import com.seasonthon.everflow.app.topic.domain.Topic;
import com.seasonthon.everflow.app.topic.domain.TopicStatus;
import com.seasonthon.everflow.app.topic.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TopicScheduler {

    private final TopicRepository topicRepository;

    // 매일 자정마다 실행
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void expireTopics() {
        LocalDateTime now = LocalDateTime.now();
        List<Topic> expired = topicRepository.findByStatusAndActiveUntilBefore(TopicStatus.ACTIVE, now);

        for (Topic t : expired) {
            t.expire(); // 엔티티에 expire() 같은 상태 변경 메서드 정의
        }
    }
}
