package com.seasonthon.everflow.app.topic.domain;

import com.seasonthon.everflow.app.global.domain.BaseTimeEntity;
import com.seasonthon.everflow.app.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "topic_answers",
        uniqueConstraints = @UniqueConstraint(name = "UK_topic_user", columnNames = {"topic_id", "user_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TopicAnswer extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 4000)
    private String content;

    @Column(name = "family_id", nullable = false)
    private Long familyId;

    @Builder
    private TopicAnswer(Topic topic, User user, String content, Long familyId) {
        this.topic = topic;
        this.user = user;
        this.content = content;
        this.familyId = familyId;
    }

    public void updateContent(String content) {
        this.content = content;
    }
}