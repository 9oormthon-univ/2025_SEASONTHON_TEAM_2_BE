package com.seasonthon.everflow.app.bookshelf.repository;

import com.seasonthon.everflow.app.bookshelf.domain.CustomBookshelfQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomBookshelfQuestionRepository extends JpaRepository<CustomBookshelfQuestion, Long> {
    List<CustomBookshelfQuestion> findAllByFamilyId(Long familyId);
    boolean existsByFamilyIdAndQuestion(Long familyId, String question);
}
