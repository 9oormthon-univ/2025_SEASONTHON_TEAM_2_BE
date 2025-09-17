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
@Table(name = "family_bookshelf")
@EntityListeners(AuditingEntityListener.class)
public class BookshelfQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;

    @Column(name = "question_text", nullable = false, length = 500, unique = true)
    private String questionText;

    @Column(name = "question_type", nullable = false, length = 20)
    private String questionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 16)
    private QuestionScope scope;

    @Column(name = "options", length = 2000)
    private String options;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "family_id")
    private Family family;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "created_by")
    private User createdBy;

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
