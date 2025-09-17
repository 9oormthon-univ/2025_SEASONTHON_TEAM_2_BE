package com.seasonthon.everflow.app.topic.domain;

import com.seasonthon.everflow.app.user.domain.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(
        name = "topic_answers",
        uniqueConstraints = @UniqueConstraint(name = "UK_topic_user", columnNames = {"topic_id","user_id"})
)
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TopicAnswer {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 4000)
    private String content;

    @Column(name = "family_id", nullable = false)
    private Long familyId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private TopicAnswer(Topic topic, User user, String content, Long familyId) {
        this.topic = topic;
        this.user = user;
        this.content = content;
        this.familyId = familyId;
        this.createdAt = LocalDateTime.now();
    }

    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }
}
