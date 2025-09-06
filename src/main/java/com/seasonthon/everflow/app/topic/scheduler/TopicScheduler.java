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

    /**
     * 매일 00:02(KST)에 실행:
     * - 활성 토픽이 없으면 가장 오래된 DRAFT 하나를 지금부터 3일간 ACTIVE로 전환
     */
    @Scheduled(cron = "0 2 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void activateIfNone() {
        LocalDateTime now = LocalDateTime.now();

        // 이미 활성 토픽이 있으면 종료
        boolean hasActive = topicRepository
                .findFirstByStatusAndActiveFromLessThanEqualAndActiveUntilGreaterThanOrderByActiveFromDesc(
                        TopicStatus.ACTIVE, now, now
                )
                .isPresent();
        if (hasActive) return;

        // 가장 오래된 DRAFT를 3일 활성화
        topicRepository.findFirstByStatusOrderByIdAsc(TopicStatus.DRAFT)
                .ifPresent(next -> next.activateAt(now, 3));
    }
}