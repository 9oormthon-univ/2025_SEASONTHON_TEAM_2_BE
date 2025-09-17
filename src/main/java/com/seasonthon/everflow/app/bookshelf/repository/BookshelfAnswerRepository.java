package com.seasonthon.everflow.app.bookshelf.repository;

import com.seasonthon.everflow.app.bookshelf.domain.BookshelfAnswer;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface BookshelfAnswerRepository extends JpaRepository<BookshelfAnswer, Long> {

    Optional<BookshelfAnswer> findByQuestionIdAndUserId(Long questionId, Long userId);

    List<BookshelfAnswer> findAllByUserIdAndQuestionIdIn(Long userId, List<Long> questionIds);

    long deleteByQuestionId(Long questionId);

    void deleteAllByUserId(Long userId);

    @Modifying
    @Query("delete from BookshelfAnswer ba where ba.question.createdBy.id = :userId")
    void deleteAllByQuestionCreatedById(@Param("userId") Long userId);
}
