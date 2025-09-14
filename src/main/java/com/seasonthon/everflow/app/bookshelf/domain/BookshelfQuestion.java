package com.seasonthon.everflow.app.bookshelf.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "family_bookshelf")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookshelfQuestion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;

    @Column(name = "question_text", nullable = false, length = 500, unique = true)
    private String questionText;

    @Column(name = "question_type", nullable = false, length = 20)
    private String questionType; // TEXT 또는 SELECT

    @Column(name = "options", length = 2000)
    private String options;      // SELECT일 때 콤마/JSON 등으로 저장

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private BookshelfQuestion(String questionText, String questionType, String options, Boolean isActive) {
        this.questionText = questionText;
        this.questionType = questionType;
        this.options = options;
        if (isActive != null) this.isActive = isActive;
        this.createdAt = LocalDateTime.now();
    }

    public void updateOptions(String options) { this.options = options; }
    public void deactivate() { this.isActive = false; this.updatedAt = LocalDateTime.now(); }
}
