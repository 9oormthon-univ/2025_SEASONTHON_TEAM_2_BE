package com.seasonthon.everflow.app.bookshelf.service;

import com.seasonthon.everflow.app.bookshelf.domain.BookshelfAnswer;
import com.seasonthon.everflow.app.bookshelf.domain.BookshelfQuestion;
import com.seasonthon.everflow.app.bookshelf.domain.CustomBookshelfAnswer;
import com.seasonthon.everflow.app.bookshelf.domain.CustomBookshelfQuestion;
import com.seasonthon.everflow.app.bookshelf.dto.BookshelfAnswersUpsertRequestDto;
import com.seasonthon.everflow.app.bookshelf.dto.BookshelfEntryDto;
import com.seasonthon.everflow.app.bookshelf.dto.BookshelfUserViewDto;
import com.seasonthon.everflow.app.bookshelf.repository.BookshelfAnswerRepository;
import com.seasonthon.everflow.app.bookshelf.repository.BookshelfQuestionRepository;
import com.seasonthon.everflow.app.bookshelf.repository.CustomBookshelfAnswerRepository;
import com.seasonthon.everflow.app.bookshelf.repository.CustomBookshelfQuestionRepository;
import com.seasonthon.everflow.app.global.code.status.ErrorStatus;
import com.seasonthon.everflow.app.global.exception.GeneralException;
import com.seasonthon.everflow.app.global.oauth.service.AuthService;
import com.seasonthon.everflow.app.user.domain.User;
import com.seasonthon.everflow.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookshelfService {

    private static final long BASE_MIN_ID = 1L;
    private static final long BASE_MAX_ID = 15L;   // 기본 질문 구간
    private static final long CUSTOM_MIN_ID = 21L; // 커스텀 질문 시작 구간 (16~20 예약)

    private final BookshelfQuestionRepository questionRepository;
    private final BookshelfAnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final AuthService authService;

    private final CustomBookshelfQuestionRepository customQuestionRepository;
    private final CustomBookshelfAnswerRepository customAnswerRepository;

    /** 내 책장 조회 (기본 질문 + 커스텀 질문 + 내 답변 매핑) */
    public BookshelfUserViewDto getMyShelf(Long meId) {
        User me = userRepository.findById(meId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        return buildUserShelf(me);
    }

    /** 남의 책장 조회: 반드시 같은 가족만 허용 */
    public BookshelfUserViewDto getUserShelf(Long requesterId, Long targetUserId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        Long requesterFamilyId = authService.getFamilyId(requesterId);
        Long targetFamilyId = authService.getFamilyId(targetUserId);
        if (!Objects.equals(requesterFamilyId, targetFamilyId)) {
            throw new GeneralException(ErrorStatus.FORBIDDEN);
        }
        return buildUserShelf(target);
    }

    /** 내 답변 일괄 저장/수정 (null/빈문자열 허용 → null로 정규화) */
    @Transactional
    public void writeMyAnswers(Long meId, BookshelfAnswersUpsertRequestDto req) {
        User me = userRepository.findById(meId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        if (req == null || req.items() == null) {
            throw new GeneralException(ErrorStatus.BOOKSHELF_INVALID_PARAMETER);
        }

        // 1) 요청 payload 수집
        Map<Long, String> payload = new HashMap<>();
        for (BookshelfAnswersUpsertRequestDto.ItemDto p : req.items()) {
            payload.put(p.questionId(), normalize(p.answer()));
        }
        if (payload.isEmpty()) return;

        // 2) ID 구간 분리
        Set<Long> baseIds = new HashSet<>();
        Set<Long> customIds = new HashSet<>();
        Set<Long> invalidIds = new HashSet<>();
        for (Long qid : payload.keySet()) {
            if (qid >= BASE_MIN_ID && qid <= BASE_MAX_ID) {
                baseIds.add(qid);
            } else if (qid >= CUSTOM_MIN_ID) {
                customIds.add(qid);
            } else {
                invalidIds.add(qid); // 0 이하 또는 16~20 예약구간
            }
        }
        if (!invalidIds.isEmpty()) {
            throw new GeneralException(ErrorStatus.BOOKSHELF_QUESTION_NOT_FOUND);
        }

        // 3) 기본 질문(1~15) 존재 검증 및 업서트
        if (!baseIds.isEmpty()) {
            List<BookshelfQuestion> baseQuestions = questionRepository.findAllByIsActiveTrueOrderByIdAsc();
            Map<Long, BookshelfQuestion> baseMap = new HashMap<>();
            for (BookshelfQuestion q : baseQuestions) baseMap.put(q.getId(), q);

            List<Long> notFoundBase = baseIds.stream().filter(id -> !baseMap.containsKey(id)).toList();
            if (!notFoundBase.isEmpty()) {
                throw new GeneralException(ErrorStatus.BOOKSHELF_QUESTION_NOT_FOUND);
            }

            for (Long qid : baseIds) {
                BookshelfQuestion q = baseMap.get(qid);
                String ans = payload.get(qid);
                BookshelfAnswer a = answerRepository.findByQuestionIdAndUserId(qid, me.getId())
                        .orElseGet(() -> BookshelfAnswer.builder()
                                .question(q)
                                .user(me)
                                .answer(null)
                                .build());
                a.updateAnswer(ans);
                answerRepository.save(a);
            }
        }

        // 4) 커스텀 질문(21~) 존재 검증(반드시 내 가족 소유) 및 업서트
        if (!customIds.isEmpty()) {
            Long familyId = authService.getFamilyId(me.getId());
            List<CustomBookshelfQuestion> myFamilyCustoms = customQuestionRepository.findAllByFamilyId(familyId);
            Map<Long, CustomBookshelfQuestion> customMap = new HashMap<>();
            for (CustomBookshelfQuestion cq : myFamilyCustoms) customMap.put(cq.getId(), cq);

            List<Long> notFoundCustom = customIds.stream().filter(id -> !customMap.containsKey(id)).toList();
            if (!notFoundCustom.isEmpty()) {
                throw new GeneralException(ErrorStatus.BOOKSHELF_QUESTION_NOT_FOUND);
            }

            for (Long qid : customIds) {
                CustomBookshelfQuestion cq = customMap.get(qid);
                String ans = payload.get(qid);
                CustomBookshelfAnswer ca = customAnswerRepository.findByQuestionIdAndUserId(qid, me.getId())
                        .orElseGet(() -> CustomBookshelfAnswer.create(cq, me, null));
                ca.updateContent(ans);
                customAnswerRepository.save(ca);
            }
        }
    }

    // 내부 도우미: 질문 + 해당 유저 답변을 합쳐 ShelfResponse 생성
    private BookshelfUserViewDto buildUserShelf(User user) {
        List<BookshelfQuestion> questions = questionRepository.findAllByIsActiveTrueOrderByIdAsc();
        List<Long> qids = questions.stream().map(BookshelfQuestion::getId).toList();

        Map<Long, BookshelfAnswer> answerMap = new HashMap<>();
        answerRepository.findAllByUserIdAndQuestionIdIn(user.getId(), qids)
                .forEach(a -> answerMap.put(a.getQuestion().getId(), a));

        List<BookshelfEntryDto> items = new ArrayList<>();
        java.time.LocalDateTime lastUpdatedAt = null;

        // 1) 기본 질문 + 답변
        for (BookshelfQuestion q : questions) {
            BookshelfAnswer a = answerMap.get(q.getId());
            if (a != null) {
                java.time.LocalDateTime t = (a.getUpdatedAt() != null) ? a.getUpdatedAt() : a.getCreatedAt();
                if (t != null && (lastUpdatedAt == null || t.isAfter(lastUpdatedAt))) {
                    lastUpdatedAt = t;
                }
            }
            items.add(new BookshelfEntryDto(q.getId(), q.getQuestionText(), a != null ? a.getAnswer() : null));
        }

        // 2) 커스텀 질문 + 답변 (ID는 실제 PK: 21부터)
        Long familyId = authService.getFamilyId(user.getId());
        List<CustomBookshelfQuestion> customQuestions = customQuestionRepository.findAllByFamilyId(familyId);
        for (CustomBookshelfQuestion cq : customQuestions) {
            CustomBookshelfAnswer ca = customAnswerRepository.findByQuestionIdAndUserId(cq.getId(), user.getId()).orElse(null);
            if (ca != null) {
                java.time.LocalDateTime t = (ca.getUpdatedAt() != null) ? ca.getUpdatedAt() : ca.getCreatedAt();
                if (t != null && (lastUpdatedAt == null || t.isAfter(lastUpdatedAt))) {
                    lastUpdatedAt = t;
                }
            }
            items.add(new BookshelfEntryDto(cq.getId(), cq.getQuestion(), ca != null ? ca.getContent() : null));
        }

        return new BookshelfUserViewDto(user.getId(), user.getNickname(), lastUpdatedAt, items);
    }

    private String normalize(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
