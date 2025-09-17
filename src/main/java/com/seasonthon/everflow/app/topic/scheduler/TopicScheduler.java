package com.seasonthon.everflow.app.topic.scheduler;

import com.seasonthon.everflow.app.topic.domain.TopicStatus;
import com.seasonthon.everflow.app.topic.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TopicScheduler {

    private final TopicRepository topicRepository;

    @Transactional
    @Scheduled(cron = "0 2 0 * * *", zone = "Asia/Seoul")
    public void activateIfNone() {
        LocalDateTime now = LocalDateTime.now();

        boolean hasActive = topicRepository
                .findFirstByStatusAndActiveFromLessThanEqualAndActiveUntilGreaterThanOrderByActiveFromDesc(
                        TopicStatus.ACTIVE, now, now
                )
                .isPresent();

        if (hasActive) {
            return;
        }

        topicRepository.findFirstByStatusOrderByIdAsc(TopicStatus.DRAFT)
                .ifPresent(next -> next.activateAt(now, 3));
    }
}
