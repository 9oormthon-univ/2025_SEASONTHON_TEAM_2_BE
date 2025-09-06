package com.seasonthon.everflow.app.bookshelf.domain;

import com.seasonthon.everflow.app.family.domain.Family;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "custom_bookshelf_questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomBookshelfQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 가족이 만든 질문인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @Column(nullable = false, length = 500)
    private String question;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Builder
    public CustomBookshelfQuestion(Family family, String question) {
        this.family = family;
        this.question = question;
        this.createdAt = java.time.LocalDateTime.now();
    }

    public void updateQuestion(String newQuestion) {
        this.question = newQuestion;
    }
}
