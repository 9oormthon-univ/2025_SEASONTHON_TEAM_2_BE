package com.seasonthon.everflow.app.appointment.controller;

import com.seasonthon.everflow.app.appointment.dto.AppointmentRequestDto;
import com.seasonthon.everflow.app.appointment.dto.AppointmentResponseDto;
import com.seasonthon.everflow.app.appointment.service.AppointmentService;
import com.seasonthon.everflow.app.global.code.dto.ApiResponse;
import com.seasonthon.everflow.app.global.code.status.SuccessStatus;
import com.seasonthon.everflow.app.global.oauth.domain.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name="Appointment API", description = "약속 관련 api")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Operation(summary = "약속등록", description = "약속을 등록하는 api입니다.")
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

    @Operation(summary = "월별 약속 조회", description = "해당 월에 약속이 등록된 날짜들을 반환합니다.")
    @GetMapping("/month")
    public ResponseEntity<ApiResponse<AppointmentResponseDto.AppointmentMonthResponseDto>> getMonthAppointment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam int year,
            @RequestParam int month) {

        AppointmentResponseDto.AppointmentMonthResponseDto resultDto =
                appointmentService.getMonthAppointment(userDetails, year, month);

        return ResponseEntity.ok(ApiResponse.of(SuccessStatus.OK, resultDto));
    }

    @Operation(summary = "일별 약속 조회", description = "해당 일자에 등록된 약속들을 반환합니다. participantNum은 외 @명에 들어가는 값으로, 총 참여자 수 -1명을 반환합니다.")
    @GetMapping("/date")
    public ResponseEntity<ApiResponse<List<AppointmentResponseDto.AppointmentDateResponseDto>>> getDateAppointment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int day) {

        List<AppointmentResponseDto.AppointmentDateResponseDto> resultDtoList =
                appointmentService.getDateAppointment(userDetails, year, month, day);

        return ResponseEntity.ok(ApiResponse.of(SuccessStatus.OK, resultDtoList));
    }

    @Operation(summary = "약속 상세조회", description = "약속을 상세 조회합니다.")
    @GetMapping("/{appointmentId}")
    public ResponseEntity<ApiResponse<AppointmentResponseDto.AppointmentDetailResponseDto>> getAppointment(
            @PathVariable Long appointmentId ) {
        AppointmentResponseDto.AppointmentDetailResponseDto resultDto =
                appointmentService.getAppointment(appointmentId);

        return ResponseEntity.ok(ApiResponse.of(SuccessStatus.OK, resultDto));
    }

    @Operation(summary = "약속삭제", description = "해당 약속을 삭제합니다.")
    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<ApiResponse<AppointmentResponseDto.MessageResponseDto>> deleteAppointment(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        AppointmentResponseDto.MessageResponseDto resultDto = appointmentService.deleteAppointment(appointmentId, userId);

        return ResponseEntity.ok(ApiResponse.of(SuccessStatus.OK, resultDto));
    }

    @Operation(summary = "약속 상태 변경", description = "해당 약속을 수락(ACCEPTED)하거나 거절(REJECTED)합니다.")
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

