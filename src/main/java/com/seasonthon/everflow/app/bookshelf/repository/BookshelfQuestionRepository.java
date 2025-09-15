package com.seasonthon.everflow.app.bookshelf.repository;

import com.seasonthon.everflow.app.bookshelf.domain.BookshelfQuestion;
import com.seasonthon.everflow.app.bookshelf.domain.QuestionScope;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookshelfQuestionRepository extends JpaRepository<BookshelfQuestion, Long> {
    Optional<BookshelfQuestion> findByQuestionText(String text);

    List<BookshelfQuestion> findAllByIsActiveTrueOrderByIdAsc();

    // 새로 추가: 스코프 기준 조회
    List<BookshelfQuestion> findAllByScope(QuestionScope scope);

    // 가족별 커스텀 질문 조회
    List<BookshelfQuestion> findAllByScopeAndFamily_Id(QuestionScope scope, Long familyId);

    // 특정 질문 존재 여부 (중복 방지)
    boolean existsByQuestionText(String questionText);
}
