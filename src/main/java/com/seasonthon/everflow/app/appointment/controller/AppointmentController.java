package com.seasonthon.everflow.app.appointment.controller;

import com.seasonthon.everflow.app.appointment.dto.AppointmentRequestDto;
import com.seasonthon.everflow.app.appointment.dto.AppointmentResponseDto;
import com.seasonthon.everflow.app.appointment.service.AppointmentService;
import com.seasonthon.everflow.app.global.oauth.domain.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/family/{familyId}")
    public ResponseEntity<AppointmentResponseDto.AppointmentMonthResponseDto> getMonthAppointment(
            @PathVariable Long familyId,
            @RequestParam int year,
            @RequestParam int month) {

        AppointmentResponseDto.AppointmentMonthResponseDto responseDto =
                appointmentService.getMonthAppointment(familyId, year, month);

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/family/{familyId}/date")
    public ResponseEntity<List<AppointmentResponseDto.AppointmentDateResponseDto>> getDateAppointment(
            @PathVariable Long familyId,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int day) {

        List<AppointmentResponseDto.AppointmentDateResponseDto> responseDtoList =
                appointmentService.getDateAppointment(familyId, year, month, day);

        return ResponseEntity.ok(responseDtoList);
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponseDto.AppointmentDetailResponseDto> getAppointment(
            @PathVariable Long appointmentId ) {
        AppointmentResponseDto.AppointmentDetailResponseDto responseDto =
                appointmentService.getAppointment(appointmentId);

        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponseDto.MessageResponseDto> deleteAppointment(@PathVariable Long appointmentId) {
        AppointmentResponseDto.MessageResponseDto responseDto = appointmentService.deleteAppointment(appointmentId);

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