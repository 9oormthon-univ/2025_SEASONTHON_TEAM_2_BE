package com.seasonthon.everflow.app.bookshelf.domain;

import com.seasonthon.everflow.app.family.domain.Family;
import com.seasonthon.everflow.app.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/* 가족 커스텀 질문 엔티티 */
@Entity
@Table(
    name = "custom_bookshelf_questions",
    indexes = {
        @Index(name = "idx_cbq_family", columnList = "family_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomBookshelfQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* 어떤 가족이 만든 질문인지 (지연 로딩) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    /* 질문을 만든 사용자 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    /* 질문 본문 (최대 500자) */
    @Column(name = "question", nullable = false, length = 500)
    private String question;

    /* 생성 시각 (DB 자동 기록) */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime(6)")
    private LocalDateTime createdAt;

    @Builder
    private CustomBookshelfQuestion(Family family, User createdBy, String question) {
        this.family = family;
        this.createdBy = createdBy;
        this.question = (question == null) ? "" : question.trim();
    }

    /* 생성 팩토리 */
    public static CustomBookshelfQuestion create(Family family, User createdBy, String question) {
        return CustomBookshelfQuestion.builder()
                .family(family)
                .createdBy(createdBy)
                .question(question)
                .build();
    }

    /* 질문 내용 수정 */
    public void updateQuestion(String newQuestion) {
        this.question = (newQuestion == null) ? "" : newQuestion.trim();
    }
}
