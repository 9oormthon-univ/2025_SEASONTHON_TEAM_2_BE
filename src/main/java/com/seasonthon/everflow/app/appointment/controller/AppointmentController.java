package com.seasonthon.everflow.app.appointment.controller;

import com.seasonthon.everflow.app.appointment.dto.AppointmentRequestDto;
import com.seasonthon.everflow.app.appointment.dto.AppointmentResponseDto;
import com.seasonthon.everflow.app.appointment.service.AppointmentService;
import com.seasonthon.everflow.app.global.oauth.domain.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.seasonthon.everflow.app.user.domain.User;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<AppointmentResponseDto.AppointmentAddResponseDto> addAppointment(
            @Valid @RequestBody AppointmentRequestDto.AppointmentAddRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        AppointmentResponseDto.AppointmentAddResponseDto responseDto = appointmentService.addAppointment(requestDto, userId);

        // 생성된 리소스의 URI를 Location 헤더에 담아 201 Created 응답 반환
        URI location = URI.create("/api/appointments/" + responseDto.getAppointmentId());

        return ResponseEntity.created(location).body(responseDto);
    }

    @GetMapping("/family/{family_id}")
    public ResponseEntity<AppointmentResponseDto.AppointmentMonthResponseDto> getMonthAppointment(
            @PathVariable Long family_id,
            @RequestParam int year,
            @RequestParam int month) {

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("월(month)은 1에서 12 사이의 값이어야 합니다.");
        }

        AppointmentResponseDto.AppointmentMonthResponseDto responseDto =
                appointmentService.getMonthAppointment(family_id, year, month);

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/family/{family_id}/date")
    public ResponseEntity<List<AppointmentResponseDto.AppointmentDateResponseDto>> getDateAppointment(
            @PathVariable Long family_id,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int day) {

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("월(month)은 1에서 12 사이의 값이어야 합니다.");
        }

        // 간단한 날짜 유효성 검사 (실제 월의 마지막 날을 확인하는 것이 더 정확합니다)
        YearMonth yearMonth = YearMonth.of(year, month);
        if (day < 1 || day > yearMonth.lengthOfMonth()) {
            throw new IllegalArgumentException("유효하지 않은 일(day)입니다.");
        }

        List<AppointmentResponseDto.AppointmentDateResponseDto> responseDtoList =
                appointmentService.getDateAppointment(family_id, year, month, day);

        return ResponseEntity.ok(responseDtoList);
    }

    @GetMapping("/{appointment_id}")
    public ResponseEntity<AppointmentResponseDto.AppointmentDetailResponseDto> getAppointment(
            @PathVariable Long appointment_id ) {
        AppointmentResponseDto.AppointmentDetailResponseDto responseDto =
                appointmentService.getAppointment(appointment_id);

        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{appointment_id}")
    public ResponseEntity<AppointmentResponseDto.MessageResponseDto> deleteAppointment(@PathVariable Long appointment_id) {
        AppointmentResponseDto.MessageResponseDto responseDto = appointmentService.deleteAppointment(appointment_id);

        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping("/{appointmentId}/participant")
    public ResponseEntity<AppointmentResponseDto.MessageResponseDto> updateParticipantStatus(
            @PathVariable Long appointmentId,
            @RequestBody AppointmentRequestDto.UpdateParticipantStatusRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();
        AppointmentResponseDto.MessageResponseDto responseDto =
                appointmentService.updateParticipantStatus(appointmentId, userId, requestDto.getAcceptStatus());

        return ResponseEntity.ok(responseDto);
    }

}