package com.seasonthon.everflow.app.home.service;

import com.seasonthon.everflow.app.bookshelf.service.BookshelfService;
import com.seasonthon.everflow.app.global.code.status.ErrorStatus;
import com.seasonthon.everflow.app.global.exception.GeneralException;
import com.seasonthon.everflow.app.global.oauth.service.AuthService;
import com.seasonthon.everflow.app.home.dto.ClosenessResponseDto;
import com.seasonthon.everflow.app.home.dto.FamilySummaryResponseDto;
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

    public ClosenessResponseDto getCloseness(Long userId, Long familyId) {
        LocalDateTime from = LocalDateTime.now().minusDays(30);

        long myCount = answerRepository.countSinceByUser(userId, from);


        if (myCount == 0) {
            return new ClosenessResponseDto(0, 0, 0, 0, familyId);
        }

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
            return new ClosenessResponseDto(0, myCount, 0, 1, familyId);
        }

        int rank = 1;
        List<Long> sorted = counts.values().stream()
                .sorted(Comparator.reverseOrder())
                .toList();

        long higherScores = counts.values().stream().filter(score -> score > myCount).count();
        rank = (int) higherScores + 1;

        int pct = (int) Math.round((myCount * 100.0) / familyMax);

        return new ClosenessResponseDto(pct, myCount, familyMax, rank, familyId);
    }

    @Transactional(readOnly = true)
    public FamilySummaryResponseDto getFamilySummary(Long userId) {
        Long familyId = authService.getFamilyId(userId);
        if (familyId == null) {
            throw new GeneralException(ErrorStatus.BOOKSHELF_FAMILY_NOT_FOUND);
        }

        List<User> members = userRepository.findAllByFamilyId(familyId);
        if (members == null || members.isEmpty()) {
            throw new GeneralException(ErrorStatus.BOOKSHELF_MEMBERS_NOT_FOUND);
        }

        List<FamilySummaryResponseDto.FamilyMemberSummary> list = members.stream()
                .map(u -> new FamilySummaryResponseDto.FamilyMemberSummary(
                        u.getId(),
                        u.getNickname(),
                        u.getShelfColor() != null ? u.getShelfColor().name() : null
                ))
                .toList();

        return new FamilySummaryResponseDto(list);
    }
}