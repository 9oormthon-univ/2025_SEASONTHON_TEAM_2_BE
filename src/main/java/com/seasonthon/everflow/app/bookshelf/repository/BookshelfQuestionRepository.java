package com.seasonthon.everflow.app.bookshelf.repository;

import com.seasonthon.everflow.app.bookshelf.domain.BookshelfQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookshelfQuestionRepository extends JpaRepository<BookshelfQuestion, Long> {
    Optional<BookshelfQuestion> findByQuestionText(String text);
    List<BookshelfQuestion> findAllByIsActiveTrueOrderByIdAsc();
}
