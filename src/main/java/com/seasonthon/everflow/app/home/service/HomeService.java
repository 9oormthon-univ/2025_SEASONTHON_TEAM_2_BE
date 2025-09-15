package com.seasonthon.everflow.app.home.service;

import com.seasonthon.everflow.app.bookshelf.service.BookshelfService;
import com.seasonthon.everflow.app.global.code.status.ErrorStatus;
import com.seasonthon.everflow.app.global.exception.GeneralException;
import com.seasonthon.everflow.app.global.oauth.service.AuthService;
import com.seasonthon.everflow.app.home.dto.HomeDto;
import com.seasonthon.everflow.app.topic.repository.TopicAnswerRepository;
import com.seasonthon.everflow.app.user.domain.User;
import com.seasonthon.everflow.app.user.repository.UserRepository;
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
    private final AuthService authService;
    private final UserRepository userRepository;
    private final BookshelfService bookshelfService;

    /**
     * 가족 친밀도(참여율) = 내 답변 수 / 가족 내 최대 답변 수 × 100
     * 기준: 최근 30일
     */
    public HomeDto.ClosenessResponse getCloseness(Long userId, Long familyId) {
        LocalDateTime from = LocalDateTime.now().minusDays(30);

        // 내 답변 수
        long myCount = answerRepository.countSinceByUser(userId, from);
        if (myCount == 0) {
            throw new GeneralException(ErrorStatus.HOME_DATA_NOT_FOUND);
        }

        // 가족 구성원별 답변 수
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

        // 순위(동점 동일 순위)
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

    /**
     * 가족 책장 목록(나 포함): userId, nickname, shelfColor 만 반환
     */
    @Transactional(readOnly = true)
    public HomeDto.FamilySummaryResponse getFamilySummary(Long userId) {
        Long familyId = authService.getFamilyId(userId);
        if (familyId == null) {
            throw new GeneralException(ErrorStatus.BOOKSHELF_FAMILY_NOT_FOUND);
        }

        List<User> members = userRepository.findAllByFamilyId(familyId);
        if (members == null || members.isEmpty()) {
            throw new GeneralException(ErrorStatus.BOOKSHELF_MEMBERS_NOT_FOUND);
        }

        List<HomeDto.FamilyMemberSummary> list = members.stream()
                .map(u -> new HomeDto.FamilyMemberSummary(
                        u.getId(),
                        u.getNickname(),
                        u.getShelfColor() != null ? u.getShelfColor().name() : null
                ))
                .toList();

        return new HomeDto.FamilySummaryResponse(list);
    }
}