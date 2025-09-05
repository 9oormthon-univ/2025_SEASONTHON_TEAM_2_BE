package com.seasonthon.everflow.app.appointment.repository;

import com.seasonthon.everflow.app.appointment.domain.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    /**
     * 특정 기간과 겹치는 모든 약속을 조회합니다.
     * (약속의 시작일이 기간 종료일보다 이전이고, 약속의 종료일이 기간 시작일보다 이후인 경우)
     */
    @Query("SELECT a FROM Appointment a " +
            "WHERE a.family.id = :familyId " +
            "AND a.startTime <= :endOfMonth " +
            "AND a.endTime >= :startOfMonth")
    List<Appointment> findAppointmentsOverlappingWithDateRange(
            @Param("familyId") Long familyId,
            @Param("startOfMonth") LocalDateTime startOfMonth,
            @Param("endOfMonth") LocalDateTime endOfMonth);

}