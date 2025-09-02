package com.seasonthon.everflow.app.appointment.controller;

import com.seasonthon.everflow.app.appointment.dto.AppointmentRequestDto;
import com.seasonthon.everflow.app.appointment.dto.AppointmentResponseDto;
import com.seasonthon.everflow.app.appointment.service.AppointmentService;
import com.seasonthon.everflow.app.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/api/appointments") // RESTful하게 복수형을 사용하는 것을 권장
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<AppointmentResponseDto> createAppointment(
            @Valid @RequestBody AppointmentRequestDto requestDto
            //@AuthenticationPrincipal User user // Spring Security를 통해 인증된 사용자 정보 가져오기
    ) {
        Long userId = 1L;
        // 현재 로그인한 사용자의 ID를 Service로 넘겨줌
        AppointmentResponseDto responseDto = appointmentService.createAppointment(requestDto, userId);

        // 생성된 리소스의 URI를 Location 헤더에 담아 201 Created 응답 반환
        URI location = URI.create("/api/appointments/" + responseDto.getAppointmentId());

        return ResponseEntity.created(location).body(responseDto);
    }
}