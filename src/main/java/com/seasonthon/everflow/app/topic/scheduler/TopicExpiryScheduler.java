package com.seasonthon.everflow.app.topic.scheduler;

import com.seasonthon.everflow.app.topic.repository.TopicRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TopicExpiryScheduler {

    private final TopicRepository topicRepository;

    /** 매일 00:00 (KST) 기준으로 activeUntil이 지난 토픽들을 만료 처리 */
    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void expireOutdatedTopics() {
        LocalDateTime now = LocalDateTime.now();
        int updated = topicRepository.bulkExpire(now);
        if (updated > 0) {
            log.info("만료 처리된 토픽: {}건 (기준: {})", updated, now);
        } else {
            log.debug("만료 처리할 토픽 없음 (기준: {})", now);
        }
    }
}