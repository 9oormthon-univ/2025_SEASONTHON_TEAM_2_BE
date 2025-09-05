package com.seasonthon.everflow.app.appointment.repository;

import com.seasonthon.everflow.app.appointment.domain.AppointmentParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppointmentParticipantRepository extends JpaRepository<AppointmentParticipant, Long> {

    /**
     * 특정 약속(appointmentId)에 참여한 특정 사용자(userId)의 참여 정보를 조회합니다.
     */
    Optional<AppointmentParticipant> findByAppointmentIdAndUserId(Long appointmentId, Long userId);
}