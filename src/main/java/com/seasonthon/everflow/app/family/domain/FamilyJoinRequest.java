package com.seasonthon.everflow.app.family.domain;

import com.seasonthon.everflow.app.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "family_join_requests")
@Getter
@NoArgsConstructor
public class FamilyJoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JoinStatus status; // PENDING, APPROVED, REJECTED

    @Column(name = "attempts")
    private int attempts;

    public void increaseAttempts() {
        this.attempts++;
    }

    public void markPending() {
        this.status = JoinStatus.PENDING;
    }

    public void approve() {
        this.status = JoinStatus.APPROVED;
    }

    public void reject() {
        this.status = JoinStatus.REJECTED;
    }

    @Builder
    public FamilyJoinRequest(Family family, User user) {
        this.family = family;
        this.user = user;
        this.status = JoinStatus.PENDING;
        this.attempts = 0;
    }
}
