package com.seasonthon.everflow.app.bookshelf.service;

import com.seasonthon.everflow.app.bookshelf.domain.BookshelfAnswer;
import com.seasonthon.everflow.app.bookshelf.domain.BookshelfQuestion;
import com.seasonthon.everflow.app.bookshelf.domain.QuestionScope;
import com.seasonthon.everflow.app.bookshelf.dto.BookshelfAnswersUpsertRequestDto;
import com.seasonthon.everflow.app.bookshelf.dto.BookshelfEntryDto;
import com.seasonthon.everflow.app.bookshelf.dto.BookshelfUserViewDto;
import com.seasonthon.everflow.app.bookshelf.dto.CustomBookshelfQuestionCreateRequestDto;
import com.seasonthon.everflow.app.bookshelf.repository.BookshelfAnswerRepository;
import com.seasonthon.everflow.app.bookshelf.repository.BookshelfQuestionRepository;
import com.seasonthon.everflow.app.family.domain.Family;
import com.seasonthon.everflow.app.global.code.status.ErrorStatus;
import com.seasonthon.everflow.app.global.exception.GeneralException;
import com.seasonthon.everflow.app.global.oauth.service.AuthService;
import com.seasonthon.everflow.app.user.domain.User;
import com.seasonthon.everflow.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookshelfService {

    private final BookshelfQuestionRepository questionRepository;
    private final BookshelfAnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final AuthService authService;

    /** 내 책장 조회 (기본 질문 + 내 가족 커스텀 질문 + 내 답변 매핑) */
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

        // 2) 질문 일괄 로딩 및 존재 검증
        List<Long> qids = new ArrayList<>(payload.keySet());
        List<BookshelfQuestion> questions = questionRepository.findAllById(qids);
        Map<Long, BookshelfQuestion> qmap = questions.stream()
                .collect(Collectors.toMap(BookshelfQuestion::getId, q -> q));
        List<Long> notFound = qids.stream().filter(id -> !qmap.containsKey(id)).toList();
        if (!notFound.isEmpty()) {
            throw new GeneralException(ErrorStatus.BOOKSHELF_QUESTION_NOT_FOUND);
        }

        // 3) 가족 권한 검증 (CUSTOM 질문은 반드시 내 가족 소유)
        Long myFamilyId = authService.getFamilyId(me.getId());
        for (BookshelfQuestion q : questions) {
            if (q.getScope() == QuestionScope.CUSTOM) {
                Family fam = q.getFamily();
                if (fam == null || fam.getId() == null || !Objects.equals(fam.getId(), myFamilyId)) {
                    throw new GeneralException(ErrorStatus.FORBIDDEN);
                }
            }
        }

        // 4) 업서트
        for (Long qid : qids) {
            BookshelfQuestion q = qmap.get(qid);
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

    /** 커스텀 질문 생성 (기존 CustomBookshelfService.createQuestion 로직 통합) */
    @Transactional
    public BookshelfEntryDto createCustomQuestion(Long userId, CustomBookshelfQuestionCreateRequestDto req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        Long familyId = authService.getFamilyId(userId);
        if (familyId == null) {
            throw new GeneralException(ErrorStatus.NOT_IN_FAMILY_YET);
        }
        Family family = user.getFamily(); // 동일 컨텍스트에서 사용

        // 유니크 제약(질문 텍스트) 선택적으로 체크
        if (questionRepository.existsByQuestionText(req.question())) {
            throw new GeneralException(ErrorStatus.DUPLICATE_RESOURCE);
        }

        // 엔티티 팩토리 사용 (BASE/CUSTOM 구분)
        BookshelfQuestion q = BookshelfQuestion.custom(
                req.question(),         // text
                "TEXT",                 // 기본 타입 가정(필요 시 req에서 받기)
                null,                   // options
                family,
                user
        );

        BookshelfQuestion saved = questionRepository.save(q);
        return new BookshelfEntryDto(saved.getId(), saved.getQuestionText(), null);
    }

    /** 커스텀 질문 삭제 (기존 CustomBookshelfService.deleteQuestion 로직 통합) */
    @Transactional
    public void deleteCustomQuestion(Long userId, Long questionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        Long familyId = authService.getFamilyId(userId);
        if (familyId == null) {
            throw new GeneralException(ErrorStatus.NOT_IN_FAMILY_YET);
        }

        BookshelfQuestion q = questionRepository.findById(questionId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.BOOKSHELF_QUESTION_NOT_FOUND));

        if (q.getScope() != QuestionScope.CUSTOM || q.getFamily() == null
                || !Objects.equals(q.getFamily().getId(), familyId)) {
            throw new GeneralException(ErrorStatus.FORBIDDEN);
        }

        // 주의: FK 제약(answers) 때문에 삭제가 거부될 수 있음. 필요 시 Answer 먼저 삭제하도록 확장.
        questionRepository.delete(q);
    }

    // 내부 도우미: 질문 + 해당 유저 답변을 합쳐 ShelfResponse 생성 (BASE + 내 가족 CUSTOM)
    private BookshelfUserViewDto buildUserShelf(User user) {
        Long familyId = authService.getFamilyId(user.getId());

        List<BookshelfQuestion> base = questionRepository.findAllByScope(QuestionScope.BASE);
        List<BookshelfQuestion> customs = (familyId != null)
                ? questionRepository.findAllByScopeAndFamily_Id(QuestionScope.CUSTOM, familyId)
                : Collections.emptyList();

        // 정렬(필요 시 id 기준)
        List<BookshelfQuestion> questions = new ArrayList<>(base.size() + customs.size());
        questions.addAll(base);
        questions.addAll(customs);
        questions.sort(Comparator.comparingLong(BookshelfQuestion::getId));

        List<Long> qids = questions.stream().map(BookshelfQuestion::getId).toList();

        Map<Long, BookshelfAnswer> answerMap = new HashMap<>();
        if (!qids.isEmpty()) {
            answerRepository.findAllByUserIdAndQuestionIdIn(user.getId(), qids)
                    .forEach(a -> answerMap.put(a.getQuestion().getId(), a));
        }

        List<BookshelfEntryDto> items = new ArrayList<>();
        LocalDateTime lastUpdatedAt = null;

        for (BookshelfQuestion q : questions) {
            BookshelfAnswer a = answerMap.get(q.getId());
            if (a != null) {
                LocalDateTime t = (a.getUpdatedAt() != null) ? a.getUpdatedAt() : a.getCreatedAt();
                if (t != null && (lastUpdatedAt == null || t.isAfter(lastUpdatedAt))) {
                    lastUpdatedAt = t;
                }
            }
            items.add(new BookshelfEntryDto(q.getId(), q.getQuestionText(), a != null ? a.getAnswer() : null));
        }

        return new BookshelfUserViewDto(user.getId(), user.getNickname(), lastUpdatedAt, items);
    }

    private String normalize(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
