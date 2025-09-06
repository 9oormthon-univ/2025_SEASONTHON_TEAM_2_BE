package com.seasonthon.everflow.app.appointment.domain;

import com.seasonthon.everflow.app.family.domain.Family;
import com.seasonthon.everflow.app.user.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table(name = "appointment")
@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propose_user_id", nullable = false)
    private User proposeUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "content")
    private String content;

    @Column(name = "location")
    private String location;

    @Column(name = "start_time", nullable = false)

    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "color")
    private AppointmentColor color;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AcceptStatus status;

    // 약속 참여자 목록과의 일대다 관계
    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AppointmentParticipant> participants = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Appointment(User proposeUser, Family family, String name, String content, String location, LocalDateTime startTime, LocalDateTime endTime, AppointmentColor color) {
        this.proposeUser = proposeUser;
        this.family = family;
        this.name = name;
        this.content = content;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.color = color;
        this.status = AcceptStatus.PENDING;
    }

    public void updateStatus(AcceptStatus status) {
        this.status = status;
    }

    public void updateDetails(String name, String content, String location, LocalDateTime startTime, LocalDateTime endTime, AppointmentColor color) {

        this.name = name;
        this.content = content;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.color = color;
    }
}