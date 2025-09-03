package com.seasonthon.everflow.app.appointment.domain;

import com.seasonthon.everflow.app.user.domain.Family;
import com.seasonthon.everflow.app.user.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;

import java.time.LocalDate;
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

    // 약속을 제안한 사용자와의 다대일 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propose_user_id", nullable = false)
    private User proposeUser;

    // 약속이 속한 가족과의 다대일 관계 (Family 엔터티가 있다고 가정)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "content") // 'comment'에서 'content'로 변경
    private String content;

    @Column(name = "location") // 'location' 필드 추가
    private String location;

    @Column(name = "start_time", nullable = false)
    private LocalDate startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDate endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "color") // 'color' 필드 추가
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
    public Appointment(User proposeUser, Family family, String name, String content, String location, LocalDate startTime, LocalDate endTime, AppointmentColor color) {
        this.proposeUser = proposeUser;
        this.family = family;
        this.name = name;
        this.content = content;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.color = color;
        this.status = AcceptStatus.PENDING; // 생성 시 기본 상태는 '대기 중'
    }

    //== 편의 메서드 ==//
    public void updateStatus(AcceptStatus status) {
        this.status = status;
    }

    public void updateDetails(String name, String content, String location, LocalDate startTime, LocalDate endTime, AppointmentColor color) {
        this.name = name;
        this.content = content;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.color = color;
    }
}