package com.seasonthon.everflow.app.appointment.controller;

import com.seasonthon.everflow.app.appointment.dto.AppointmentRequestDto;
import com.seasonthon.everflow.app.appointment.dto.AppointmentResponseDto;
import com.seasonthon.everflow.app.appointment.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<AppointmentResponseDto.AppointmentAddResponseDto> addAppointment(
            @Valid @RequestBody AppointmentRequestDto requestDto
            //@AuthenticationPrincipal User user // Spring Security를 통해 인증된 사용자 정보 가져오기
    ) {
        Long userId = 1L;
        // 현재 로그인한 사용자의 ID를 Service로 넘겨줌
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

        // 간단한 유효성 검사 (필요에 따라 추가)
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("월(month)은 1에서 12 사이의 값이어야 합니다.");
        }

        AppointmentResponseDto.AppointmentMonthResponseDto responseDto =
                appointmentService.getMonthAppointment(familyId, year, month);

        return ResponseEntity.ok(responseDto);
    }

}