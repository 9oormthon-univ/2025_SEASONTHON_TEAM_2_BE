package com.seasonthon.everflow.app.bookshelf.repository;

import com.seasonthon.everflow.app.bookshelf.domain.BookshelfAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookshelfAnswerRepository extends JpaRepository<BookshelfAnswer, Long> {

    Optional<BookshelfAnswer> findByQuestionIdAndUserId(Long questionId, Long userId);

    List<BookshelfAnswer> findAllByUserIdAndQuestionIdIn(Long userId, List<Long> questionIds);

    long deleteByQuestionId(Long questionId);
}