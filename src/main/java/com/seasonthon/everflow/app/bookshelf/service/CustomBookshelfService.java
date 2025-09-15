package com.seasonthon.everflow.app.bookshelf.service;

import com.seasonthon.everflow.app.bookshelf.domain.CustomBookshelfQuestion;
import com.seasonthon.everflow.app.bookshelf.dto.BookshelfEntryDto;
import com.seasonthon.everflow.app.bookshelf.dto.CustomBookshelfQuestionCreateRequestDto;
import com.seasonthon.everflow.app.bookshelf.repository.CustomBookshelfQuestionRepository;
import com.seasonthon.everflow.app.family.domain.Family;
import com.seasonthon.everflow.app.global.code.status.ErrorStatus;
import com.seasonthon.everflow.app.global.exception.GeneralException;
import com.seasonthon.everflow.app.user.domain.User;
import com.seasonthon.everflow.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomBookshelfService {

    private final UserRepository userRepository;
    private final CustomBookshelfQuestionRepository questionRepository;

    @Transactional
    public BookshelfEntryDto createQuestion(Long userId, CustomBookshelfQuestionCreateRequestDto req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        Family family = user.getFamily();
        if (family == null) {
            throw new GeneralException(ErrorStatus.NOT_IN_FAMILY_YET);
        }

        CustomBookshelfQuestion question = CustomBookshelfQuestion.create(family, user, req.question());
        CustomBookshelfQuestion saved = questionRepository.save(question);

        return new BookshelfEntryDto(
                saved.getId(),
                saved.getQuestion(),
                null
        );
    }

    /**
     * 커스텀 질문 삭제
     * - 같은 가족 가족만 삭제 가능
     */
    @Transactional
    public void deleteQuestion(Long userId, Long questionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        Family family = user.getFamily();
        if (family == null) {
            throw new GeneralException(ErrorStatus.NOT_IN_FAMILY_YET);
        }

        CustomBookshelfQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.BOOKSHELF_QUESTION_NOT_FOUND));

        if (!question.getFamily().getId().equals(family.getId())) {
            throw new GeneralException(ErrorStatus.FORBIDDEN);
        }

        // FK 제약으로 인해 답변 레코드가 남아있다면 DB 설정에 따라 삭제가 거부될 수 있습니다.
        // 필요 시, AnswerRepository.deleteAllByQuestionId(questionId) 등을 먼저 수행하도록 확장하세요.
        questionRepository.delete(question);
    }
}
