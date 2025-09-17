package com.seasonthon.everflow.app.topic.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.*;

@Entity
@Table(name = "topics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String question;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TopicStatus status;

    @Column(name = "active_from", nullable = false)
    private LocalDateTime activeFrom;

    @Column(name = "active_until", nullable = false)
    private LocalDateTime activeUntil;

    @Enumerated(EnumType.STRING)
    @Column(name = "topic_type", nullable = false)
    private TopicType topicType;

    @Transient
    private Integer cachedDaysLeft;

    @Builder
    private Topic(String question, LocalDateTime activeFrom, LocalDateTime activeUntil, TopicType type) {
        this.question = question;
        this.status = TopicStatus.DRAFT;
        this.activeFrom = activeFrom;
        this.activeUntil = activeUntil;
        this.topicType = type;
    }

    public void activate() {
        this.status = TopicStatus.ACTIVE;
    }

    public void updateQuestion(String q) {
        if (q != null) this.question = q;
    }

    public void expire() {
        this.status = TopicStatus.EXPIRED;
        this.cachedDaysLeft = 0;
    }

    private int calculateDaysLeft() {
        return (int) Math.max(0,
                ChronoUnit.DAYS.between(LocalDate.now(), this.activeUntil.toLocalDate()));
    }


    public int getDaysLeft() {
        return (cachedDaysLeft != null) ? cachedDaysLeft : calculateDaysLeft();
    }


    public int refreshRemainingDays() {
        this.cachedDaysLeft = calculateDaysLeft();
        return this.cachedDaysLeft;
    }


    public void activateAt(LocalDateTime from, int days) {
        if (from == null) from = LocalDateTime.now();
        if (days <= 0) days = 3;
        this.activeFrom = from;
        this.activeUntil = from.plusDays(days);
        this.status = TopicStatus.ACTIVE;
    }
}
