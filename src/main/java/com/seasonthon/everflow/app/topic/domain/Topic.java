package com.seasonthon.everflow.app.topic.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "topics")
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Topic {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String question;                // 오늘의 질문 내용

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TopicStatus status;             // DRAFT/ACTIVE/INACTIVE

    @Column(name = "active_from", nullable = false)
    private LocalDateTime activeFrom;       // 활성 시작(포함)

    @Column(name = "active_until", nullable = false)
    private LocalDateTime activeUntil;      // 비활성 시작(미포함) = from + 3일

    @Builder
    private Topic(String question, LocalDateTime activeFrom, LocalDateTime activeUntil) {
        this.question = question;
        this.status = TopicStatus.DRAFT;
        this.activeFrom = activeFrom;
        this.activeUntil = activeUntil;
    }

    public void activate() { this.status = TopicStatus.ACTIVE; }
    public void updateQuestion(String q) { if (q != null) this.question = q; }
}