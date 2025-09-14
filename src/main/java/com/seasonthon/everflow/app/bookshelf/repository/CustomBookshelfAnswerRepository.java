package com.seasonthon.everflow.app.bookshelf.repository;

import com.seasonthon.everflow.app.bookshelf.domain.CustomBookshelfAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomBookshelfAnswerRepository extends JpaRepository<CustomBookshelfAnswer, Long> {

    Optional<CustomBookshelfAnswer> findByQuestionIdAndUserId(Long questionId, Long userId);

    boolean existsByQuestionIdAndUserId(Long questionId, Long userId);
}
