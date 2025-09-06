package com.seasonthon.everflow.app.bookshelf.service;

import com.seasonthon.everflow.app.bookshelf.domain.BookshelfAnswer;
import com.seasonthon.everflow.app.bookshelf.domain.BookshelfQuestion;
import com.seasonthon.everflow.app.bookshelf.dto.BookshelfDto;
import com.seasonthon.everflow.app.bookshelf.repository.BookshelfAnswerRepository;
import com.seasonthon.everflow.app.bookshelf.repository.BookshelfQuestionRepository;
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

    private final BookshelfQuestionRepository questionRepository;
    private final BookshelfAnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final AuthService authService;

    /** 내 책장 조회 (기본 질문 15개 + 내 답변 매핑) */
    public BookshelfDto.UserShelfResponse getMyShelf(Long meId) {
        User me = userRepository.findById(meId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        return buildUserShelf(me);
    }

    /** 남의 책장 조회: 반드시 같은 가족만 허용 */
    public BookshelfDto.UserShelfResponse getUserShelf(Long requesterId, Long targetUserId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        // 가족 여부 확인: AuthService를 통해 가족 ID 조회 (없으면 내부에서 예외 발생)
        Long requesterFamilyId = authService.getFamilyId(requesterId);
        Long targetFamilyId = authService.getFamilyId(targetUserId);

        // 같은 가족이 아니면 접근 불가 처리 (전용 에러코드가 없다면 FAMILY_NOT_FOUND로 통일)
        if (!Objects.equals(requesterFamilyId, targetFamilyId)) {
            throw new GeneralException(ErrorStatus.FAMILY_NOT_FOUND);
        }

        return buildUserShelf(target);
    }

    /** 내 답변 일괄 저장/수정 (null 답변 허용) */

    @Transactional
    public void writeMyAnswers(Long meId, BookshelfDto.WriteAnswersRequest req) {
        User me = userRepository.findById(meId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        // 유효성 체크: 요청 바디/항목 없음
        if (req == null || req.items() == null) {
            throw new GeneralException(ErrorStatus.BOOKSHELF_INVALID_PARAMETER);
        }

        // 1) 활성화된 기본 질문 전체(15개) 로드
        List<BookshelfQuestion> questions = questionRepository.findAllByIsActiveTrueOrderByIdAsc();
        Map<Long, BookshelfQuestion> qmap = new HashMap<>();
        for (BookshelfQuestion q : questions) qmap.put(q.getId(), q);

        // 기본 질문이 하나도 없으면 저장 불가
        if (qmap.isEmpty()) {
            throw new GeneralException(ErrorStatus.BOOKSHELF_INVALID_PARAMETER);
        }
        // 요청에 알 수 없는 questionId가 포함되었는지 검증
        for (BookshelfDto.WriteAnswersRequest.AnswerPair p : req.items()) {
            if (!qmap.containsKey(p.questionId())) {
                throw new GeneralException(ErrorStatus.BOOKSHELF_INVALID_PARAMETER);
            }
        }

        // 2) 클라이언트가 보낸 값 맵으로
        Map<Long, String> payload = new HashMap<>();
        for (BookshelfDto.WriteAnswersRequest.AnswerPair p : req.items()) {
            payload.put(p.questionId(), p.answer()); // answer 는 null 허용
        }

        // 3) 전체 질문을 돌며 없으면 null 답변으로 업서트
        for (BookshelfQuestion q : questions) {
            String ans = payload.getOrDefault(q.getId(), null);
            if (ans != null && ans.isBlank()) {
                ans = null; // 빈 문자열은 null로 처리
            }

            BookshelfAnswer a = answerRepository.findByQuestionIdAndUserId(q.getId(), me.getId())
                    .orElseGet(() -> BookshelfAnswer.builder()
                            .question(q)
                            .user(me)
                            .answer(null)
                            .build());

            a.updateAnswer(ans); // null 도 저장
            answerRepository.save(a);
        }
    }

    // 내부 도우미: 질문 + 해당 유저 답변을 합쳐 ShelfResponse 생성
    private BookshelfDto.UserShelfResponse buildUserShelf(User user) {
        List<BookshelfQuestion> questions = questionRepository.findAllByIsActiveTrueOrderByIdAsc();
        List<Long> qids = questions.stream().map(BookshelfQuestion::getId).toList();

        Map<Long, BookshelfAnswer> answerMap = new HashMap<>();
        answerRepository.findAllByUserIdAndQuestionIdIn(user.getId(), qids)
                .forEach(a -> answerMap.put(a.getQuestion().getId(), a));

        List<BookshelfDto.ShelfItem> items = new ArrayList<>(questions.size());
        java.time.LocalDateTime lastUpdatedAt = null;
        for (BookshelfQuestion q : questions) {
            BookshelfAnswer a = answerMap.get(q.getId());

            // 최신 시각: updatedAt이 있으면 그걸, 없으면 createdAt을 사용
            if (a != null) {
                java.time.LocalDateTime t = (a.getUpdatedAt() != null) ? a.getUpdatedAt() : a.getCreatedAt();
                if (t != null && (lastUpdatedAt == null || t.isAfter(lastUpdatedAt))) {
                    lastUpdatedAt = t;
                }
            }

            items.add(new BookshelfDto.ShelfItem(
                    q.getId(),
                    q.getQuestionText(),
                    a != null ? a.getAnswer() : null
            ));
        }

        return new BookshelfDto.UserShelfResponse(user.getId(), user.getNickname(), lastUpdatedAt, items);
    }
}
