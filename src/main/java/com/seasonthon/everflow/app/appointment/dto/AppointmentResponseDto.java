package com.seasonthon.everflow.app.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AppointmentResponseDto {
    private Long appointmentId;
    private String appointmentName;
}