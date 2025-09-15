package com.seasonthon.everflow.app.bookshelf.domain;

import com.seasonthon.everflow.app.family.domain.Family;
import com.seasonthon.everflow.app.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "family_bookshelf") // ← DB 테이블명이 이거면 맞추세요
@EntityListeners(AuditingEntityListener.class)
public class BookshelfQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;

    @Column(name = "question_text", nullable = false, length = 500, unique = true)
    private String questionText;

    // TEXT / SELECT (당장은 문자열로 유지, 나중에 enum으로 뺄 수 있음)
    @Column(name = "question_type", nullable = false, length = 20)
    private String questionType;

    // BASE / CUSTOM
    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 16)
    private QuestionScope scope;

    // SELECT일 때 옵션(콤마/JSON 등)
    @Column(name = "options", length = 2000)
    private String options;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // 커스텀 질문 메타
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "family_id")
    private Family family;        // BASE일 때는 null

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "created_by")
    private User createdBy;       // BASE일 때는 null

    // 감사 필드 (스프링 데이터 JPA Auditing)
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private BookshelfQuestion(String questionText,
                              String questionType,
                              String options,
                              boolean isActive,
                              QuestionScope scope,
                              Family family,
                              User createdBy) {
        this.questionText = questionText;
        this.questionType = questionType;
        this.options = options;
        this.isActive = isActive;
        this.scope = scope;
        this.family = family;
        this.createdBy = createdBy;
    }

    // 팩토리 메서드: 기본 질문
    public static BookshelfQuestion base(String text, String type, String options) {
        return BookshelfQuestion.builder()
                .questionText(text)
                .questionType(type)
                .options(options)
                .isActive(true)
                .scope(QuestionScope.BASE)
                .family(null)
                .createdBy(null)
                .build();
    }

    // 팩토리 메서드: 커스텀 질문
    public static BookshelfQuestion custom(String text, String type, String options,
                                           Family family, User createdBy) {
        return BookshelfQuestion.builder()
                .questionText(text)
                .questionType(type)
                .options(options)
                .isActive(true)
                .scope(QuestionScope.CUSTOM)
                .family(family)
                .createdBy(createdBy)
                .build();
    }

    public void updateOptions(String options) {
        this.options = options;
    }

    public void deactivate() {
        this.isActive = false;
    }
}