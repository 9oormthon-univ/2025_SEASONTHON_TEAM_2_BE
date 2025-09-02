package com.seasonthon.everflow.app.appointment.repository;

import com.seasonthon.everflow.app.appointment.domain.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
}