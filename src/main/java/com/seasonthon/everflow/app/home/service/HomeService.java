package com.seasonthon.everflow.app.home.service;

import com.seasonthon.everflow.app.home.dto.HomeDto;
import com.seasonthon.everflow.app.topic.repository.TopicAnswerRepository;
import com.seasonthon.everflow.app.global.exception.GeneralException;
import com.seasonthon.everflow.app.global.code.status.ErrorStatus;
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
     * 항상 최근 30일(LAST_30D) 기준으로 계산
     */
    public HomeDto.ClosenessResponse getCloseness(Long userId, Long familyId) {
        LocalDateTime from = LocalDateTime.now().minusDays(30);

        // 내 답변 수 조회
        long myCount = answerRepository.countSinceByUser(userId, from);
        if (myCount == 0) {
            throw new GeneralException(ErrorStatus.HOME_DATA_NOT_FOUND);
        }

        // 가족 구성원별 답변 수 조회
        List<Object[]> grouped = answerRepository.countSinceByFamilyGroup(familyId, from);
        if (grouped.isEmpty()) {
            throw new GeneralException(ErrorStatus.FAMILY_PARTICIPATION_NOT_FOUND);
        }

        long familyMax = 0;
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : grouped) {
            Long uid = (Long) row[0];
            long cnt = ((Number) row[1]).longValue();
            counts.put(uid, cnt);
            familyMax = Math.max(familyMax, cnt);
        }

        if (familyMax == 0) {
            throw new GeneralException(ErrorStatus.HOME_DATA_NOT_FOUND);
        }

        // 순위 계산 (동점은 동일 순위)
        int rank = 1;
        List<Long> sorted = counts.values().stream()
                .sorted(Comparator.reverseOrder())
                .toList();
        for (int i = 0; i < sorted.size(); i++) {
            if (Objects.equals(sorted.get(i), myCount)) {
                rank = i + 1;
                break;
            }
        }

        int pct = (int) Math.round((myCount * 100.0) / familyMax);

        return new HomeDto.ClosenessResponse(pct, myCount, familyMax, rank, familyId);
    }
}