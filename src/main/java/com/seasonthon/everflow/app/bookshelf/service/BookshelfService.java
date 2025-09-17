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

    public BookshelfUserViewDto getMyShelf(Long meId) {
        User me = userRepository.findById(meId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        return buildUserShelf(me);
    }

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

    @Transactional
    public void writeMyAnswers(Long meId, BookshelfAnswersUpsertRequestDto req) {
        User me = userRepository.findById(meId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        if (req == null || req.items() == null) {
            throw new GeneralException(ErrorStatus.BOOKSHELF_INVALID_PARAMETER);
        }

        Map<Long, String> payload = new HashMap<>();
        for (BookshelfAnswersUpsertRequestDto.ItemDto p : req.items()) {
            payload.put(p.questionId(), normalize(p.answer()));
        }
        if (payload.isEmpty()) return;

        List<Long> qids = new ArrayList<>(payload.keySet());
        List<BookshelfQuestion> questions = questionRepository.findAllById(qids);
        Map<Long, BookshelfQuestion> qmap = questions.stream()
                .collect(Collectors.toMap(BookshelfQuestion::getId, q -> q));
        List<Long> notFound = qids.stream().filter(id -> !qmap.containsKey(id)).toList();
        if (!notFound.isEmpty()) {
            throw new GeneralException(ErrorStatus.BOOKSHELF_QUESTION_NOT_FOUND);
        }

        Long myFamilyId = authService.getFamilyId(me.getId());
        for (BookshelfQuestion q : questions) {
            if (q.getScope() == QuestionScope.CUSTOM) {
                Family fam = q.getFamily();
                if (fam == null || fam.getId() == null || !Objects.equals(fam.getId(), myFamilyId)) {
                    throw new GeneralException(ErrorStatus.FORBIDDEN);
                }
            }
        }

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

    @Transactional
    public BookshelfEntryDto createCustomQuestion(Long userId, CustomBookshelfQuestionCreateRequestDto req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        Long familyId = authService.getFamilyId(userId);
        if (familyId == null) {
            throw new GeneralException(ErrorStatus.NOT_IN_FAMILY_YET);
        }
        Family family = user.getFamily();

        if (questionRepository.existsByQuestionText(req.question())) {
            throw new GeneralException(ErrorStatus.DUPLICATE_RESOURCE);
        }

        BookshelfQuestion q = BookshelfQuestion.custom(
                req.question(),
                "TEXT",
                null,
                family,
                user
        );

        BookshelfQuestion saved = questionRepository.save(q);
        return new BookshelfEntryDto(saved.getId(), saved.getQuestionText(), null);
    }

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

        answerRepository.deleteByQuestionId(questionId);
        questionRepository.delete(q);
    }

    private BookshelfUserViewDto buildUserShelf(User user) {
        Long familyId = authService.getFamilyId(user.getId());

        List<BookshelfQuestion> base = questionRepository.findAllByScope(QuestionScope.BASE);
        List<BookshelfQuestion> customs = (familyId != null)
                ? questionRepository.findAllByScopeAndFamily_Id(QuestionScope.CUSTOM, familyId)
                : Collections.emptyList();

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
