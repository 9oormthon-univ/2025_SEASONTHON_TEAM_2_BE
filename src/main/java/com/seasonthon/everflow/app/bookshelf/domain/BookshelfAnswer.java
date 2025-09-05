package com.seasonthon.everflow.app.bookshelf.domain;

import com.seasonthon.everflow.app.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "bookshelf_answers",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_bookshelf_question_user",
                columnNames = {"question_id", "user_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookshelfAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 기본 가족 책장 질문(고정 15문항)에 대한 참조 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private BookshelfQuestion question;

    /** 답변 작성자 (User → Family를 통해 가족 식별 가능) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 답변 내용 (null 허용) */
    @Column(columnDefinition = "TEXT", nullable = true)
    private String answer;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private BookshelfAnswer(BookshelfQuestion question, User user, String answer) {
        this.question = question;
        this.user = user;
        this.answer = answer; // null 허용
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updateAnswer(String newAnswer) {
        this.answer = newAnswer; // null 가능
    }
}
