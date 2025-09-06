package com.seasonthon.everflow.app.bookshelf.repository;

import com.seasonthon.everflow.app.bookshelf.domain.BookshelfAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookshelfAnswerRepository extends JpaRepository<BookshelfAnswer, Long> {

    // 특정 질문 + 사용자에 대한 답변
    Optional<BookshelfAnswer> findByQuestionIdAndUserId(Long questionId, Long userId);

    // 특정 사용자 + 여러 질문 id에 대한 답변들
    List<BookshelfAnswer> findAllByUserIdAndQuestionIdIn(Long userId, List<Long> questionIds);
}