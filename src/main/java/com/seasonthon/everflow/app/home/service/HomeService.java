package com.seasonthon.everflow.app.home.service;

import com.seasonthon.everflow.app.home.dto.HomeDto;
import com.seasonthon.everflow.app.topic.domain.TopicStatus;
import com.seasonthon.everflow.app.topic.repository.TopicAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private final TopicAnswerRepository answerRepository;

    /**
     * 가족 친밀도(참여율) = 내 답변 수 / 가족 내 최대 답변 수 × 100
     * scope: ALL(누적, 기본) | ACTIVE(현재 활성 토픽만) | LAST_30D(최근 30일)
     */
    public HomeDto.ClosenessResponse getCloseness(Long userId, Long familyId, String scopeRaw) {
        String scope = (scopeRaw == null) ? "ALL" : scopeRaw.toUpperCase(Locale.ROOT);

        long myCount;
        List<Object[]> grouped; // [userId, cnt]

        switch (scope) {
            case "ACTIVE" -> {
                myCount = answerRepository.countActiveByUser(userId, TopicStatus.ACTIVE);
                grouped = answerRepository.countActiveByFamilyGroup(familyId, TopicStatus.ACTIVE);
            }
            case "LAST_30D" -> {
                LocalDateTime from = LocalDateTime.now().minusDays(30);
                myCount = answerRepository.countSinceByUser(userId, from);
                grouped = answerRepository.countSinceByFamilyGroup(familyId, from);
            }
            default -> { // ALL
                myCount = answerRepository.countByUserId(userId);
                grouped = answerRepository.countByFamilyGroup(familyId);
            }
        }

        long familyMax = 0;
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : grouped) {
            Long uid = (Long) row[0];
            long cnt = ((Number) row[1]).longValue();
            counts.put(uid, cnt);
            familyMax = Math.max(familyMax, cnt);
        }

        // 순위 계산(동점은 동일 순위로 동일값의 첫 인덱스를 순위로)
        int rank = 1;
        List<Long> sorted = counts.values().stream()
                .sorted(Comparator.reverseOrder())
                .toList();
        for (int i = 0; i < sorted.size(); i++) {
            if (Objects.equals(sorted.get(i), myCount)) { rank = i + 1; break; }
        }

        int pct = (familyMax == 0) ? 0 : (int)Math.round((myCount * 100.0) / familyMax);
        return new HomeDto.ClosenessResponse(pct, myCount, familyMax, rank, familyId, scope);
    }
}