package com.seasonthon.everflow.app.appointment.domain;

import com.seasonthon.everflow.app.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Table(name = "appointment_participant")
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 약속에 속해있는지 (다대일 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    // 어떤 유저가 참여하는지 (다대일 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "accept_status")
    private AcceptStatus acceptStatus; // PENDING, ACCEPTED, REJECTED

    public void updateStatus(AcceptStatus acceptStatus) {
        this.acceptStatus = acceptStatus;
    }
}