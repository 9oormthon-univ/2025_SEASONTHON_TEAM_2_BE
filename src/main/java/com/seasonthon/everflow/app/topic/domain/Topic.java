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
    private String question;                // 오늘의 질문 문구

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TopicStatus status;             // DRAFT / ACTIVE / EXPIRED

    @Column(name = "active_from", nullable = false)
    private LocalDateTime activeFrom;       // 활성 시작(포함)

    @Column(name = "active_until", nullable = false)
    private LocalDateTime activeUntil;      // 비활성 시작(미포함) = from + 3일

    @Enumerated(EnumType.STRING)
    @Column(name = "topic_type", nullable = false)
    private TopicType topicType;            // CASUAL / CLOSER / DEEP

    // DB에 저장하지 않는 조회용 캐시 필드
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

    /** 활성 상태로 전환 */
    public void activate() {
        this.status = TopicStatus.ACTIVE;
    }

    /** 질문 문구 수정 */
    public void updateQuestion(String q) {
        if (q != null) this.question = q;
    }

    /** 만료 처리 */
    public void expire() {
        this.status = TopicStatus.EXPIRED;
        this.cachedDaysLeft = 0; // 조회 캐시도 함께 0으로
    }

    /** 오늘 기준 남은 일수 계산 함수(음수 방지) */
    private int calculateDaysLeft() {
        return (int) Math.max(0,
                ChronoUnit.DAYS.between(LocalDate.now(), this.activeUntil.toLocalDate()));
    }

    /**
     * 조회용 남은 일수(캐시 사용)
     * - 캐시가 없으면 즉시 계산해서 반환
     */
    public int getDaysLeft() {
        return (cachedDaysLeft != null) ? cachedDaysLeft : calculateDaysLeft();
    }

    /**
     * 남은 일수를 즉시 재계산하여 캐시에 반영하고 값을 반환
     * - 서비스 조회 직전에 호출해서 최신 값으로 응답하고 싶을 때 사용
     */
    public int refreshRemainingDays() {
        this.cachedDaysLeft = calculateDaysLeft();
        return this.cachedDaysLeft;
    }

    /**
     * 지정된 시작시각부터 'days'일 동안 활성화로 전환
     * (activeFrom/activeUntil을 함께 세팅)
     */
    public void activateAt(LocalDateTime from, int days) {
        if (from == null) from = LocalDateTime.now();
        if (days <= 0) days = 3;
        this.activeFrom = from;
        this.activeUntil = from.plusDays(days);
        this.status = TopicStatus.ACTIVE;
    }
}