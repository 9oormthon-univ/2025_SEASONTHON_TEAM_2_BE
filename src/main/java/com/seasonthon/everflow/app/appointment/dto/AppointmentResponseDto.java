package com.seasonthon.everflow.app.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

public class AppointmentResponseDto{

    @Getter
    @AllArgsConstructor
    public static class AppointmentAddResponseDto {
        private Long appointmentId;
        private String appointmentName;
    }

    @Getter
    @AllArgsConstructor
    public static class AppointmentMonthResponseDto {
        private List<String> dates;
    }

    @Getter
    @AllArgsConstructor
    public static class AppointmentDetailResponseDto {
        private Long appointmentId;
        private String appointmentName;
        private String startTime;
        private String endTime;
        private String location;
        private String content;
        private String proposeUserName;

    }
}
