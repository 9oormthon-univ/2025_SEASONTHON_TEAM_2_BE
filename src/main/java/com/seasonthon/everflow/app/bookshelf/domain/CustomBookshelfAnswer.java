package com.seasonthon.everflow.app.bookshelf.domain;

import com.seasonthon.everflow.app.user.domain.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "custom_bookshelf_answer", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"question_id", "user_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomBookshelfAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private CustomBookshelfQuestion question;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "content", columnDefinition = "TEXT", nullable = true)
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime(6)")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "datetime(6)")
    private LocalDateTime updatedAt;

    public static CustomBookshelfAnswer create(CustomBookshelfQuestion question, User user, String content) {
        CustomBookshelfAnswer a = new CustomBookshelfAnswer();
        a.question = question;
        a.user = user;
        a.content = (content == null || content.isBlank()) ? null : content;
        return a;
    }

    public void updateContent(String newContent) {
        this.content = (newContent == null || newContent.isBlank()) ? null : newContent;
    }
}
