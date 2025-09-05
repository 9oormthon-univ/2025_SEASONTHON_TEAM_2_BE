package com.seasonthon.everflow.app.appointment.controller;

import com.seasonthon.everflow.app.appointment.dto.AppointmentRequestDto;
import com.seasonthon.everflow.app.appointment.dto.AppointmentResponseDto;
import com.seasonthon.everflow.app.appointment.service.AppointmentService;
import com.seasonthon.everflow.app.global.code.dto.ApiResponse;
import com.seasonthon.everflow.app.global.code.status.SuccessStatus;
import com.seasonthon.everflow.app.global.oauth.domain.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentResponseDto.AppointmentAddResponseDto>> addAppointment(
            @Valid @RequestBody AppointmentRequestDto.AppointmentAddRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        AppointmentResponseDto.AppointmentAddResponseDto resultDto = appointmentService.addAppointment(requestDto, userId);

        URI location = URI.create("/api/appointments/" + resultDto.getAppointmentId());

        ApiResponse<AppointmentResponseDto.AppointmentAddResponseDto> responseBody =
                ApiResponse.of(SuccessStatus.CREATED, resultDto);

        return ResponseEntity.created(location).body(responseBody);
    }

    @GetMapping("/family/{familyId}")
    public ResponseEntity<ApiResponse<AppointmentResponseDto.AppointmentMonthResponseDto>> getMonthAppointment(
            @PathVariable Long familyId,
            @RequestParam int year,
            @RequestParam int month) {

        AppointmentResponseDto.AppointmentMonthResponseDto resultDto =
                appointmentService.getMonthAppointment(familyId, year, month);

        return ResponseEntity.ok(ApiResponse.of(SuccessStatus.OK, resultDto));
    }

    @GetMapping("/family/{familyId}/date")
    public ResponseEntity<ApiResponse<List<AppointmentResponseDto.AppointmentDateResponseDto>>> getDateAppointment(
            @PathVariable Long familyId,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int day) {

        List<AppointmentResponseDto.AppointmentDateResponseDto> resultDtoList =
                appointmentService.getDateAppointment(familyId, year, month, day);

        return ResponseEntity.ok(ApiResponse.of(SuccessStatus.OK, resultDtoList));
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<ApiResponse<AppointmentResponseDto.AppointmentDetailResponseDto>> getAppointment(
            @PathVariable Long appointmentId ) {
        AppointmentResponseDto.AppointmentDetailResponseDto resultDto =
                appointmentService.getAppointment(appointmentId);

        return ResponseEntity.ok(ApiResponse.of(SuccessStatus.OK, resultDto));
    }

    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<ApiResponse<AppointmentResponseDto.MessageResponseDto>> deleteAppointment(@PathVariable Long appointmentId) {
        AppointmentResponseDto.MessageResponseDto resultDto = appointmentService.deleteAppointment(appointmentId);

        return ResponseEntity.ok(ApiResponse.of(SuccessStatus.OK, resultDto));
    }

    @PatchMapping("/{appointmentId}/participant")
    public ResponseEntity<ApiResponse<AppointmentResponseDto.MessageResponseDto>> updateParticipantStatus(
            @PathVariable Long appointmentId,
            @RequestBody AppointmentRequestDto.UpdateParticipantStatusRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();
        AppointmentResponseDto.MessageResponseDto resultDto =
                appointmentService.updateParticipantStatus(appointmentId, userId, requestDto.getAcceptStatus());

        return ResponseEntity.ok(ApiResponse.of(SuccessStatus.OK, resultDto));
    }

}

