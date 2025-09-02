package com.seasonthon.everflow.app.appointment.service;

import com.seasonthon.everflow.app.appointment.domain.AcceptStatus;
import com.seasonthon.everflow.app.appointment.domain.Appointment;
import com.seasonthon.everflow.app.appointment.domain.AppointmentParticipant;
import com.seasonthon.everflow.app.appointment.dto.AppointmentRequestDto;
import com.seasonthon.everflow.app.appointment.dto.AppointmentResponseDto;
import com.seasonthon.everflow.app.appointment.repository.AppointmentRepository;
import com.seasonthon.everflow.app.user.domain.User;
import com.seasonthon.everflow.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository; // User 정보를 가져오기 위해 필요

    @Transactional
    public AppointmentResponseDto createAppointment(AppointmentRequestDto requestDto, Long proposeUserId) {
        // 1. 약속을 제안한 사용자(proposeUser) 정보를 DB에서 조회
        User proposeUser = userRepository.findById(proposeUserId)
                .orElseThrow(() -> new IllegalArgumentException("제안자를 찾을 수 없습니다."));

        // 2. Appointment 엔터티 생성
        Appointment appointment = Appointment.builder()
                .proposeUser(proposeUser)
                .family(proposeUser.getFamily()) // 제안자의 가족 정보를 가져옴
                .name(requestDto.getName())
                .content(requestDto.getContent())
                .location(requestDto.getLocation())
                .startTime(requestDto.getStartTime())
                .endTime(requestDto.getEndTime())
                .color(requestDto.getColor())
                .build();

        // 3. 참여자(participant) 정보 생성
        List<User> participants = userRepository.findAllById(requestDto.getParticipantUserIds());
        if (participants.size() != requestDto.getParticipantUserIds().size()) {
            throw new IllegalArgumentException("참여자를 찾을 수 없습니다.");
        }

        List<AppointmentParticipant> appointmentParticipants = participants.stream()
                .map(participantUser -> AppointmentParticipant.builder()
                        .appointment(appointment)
                        .user(participantUser)
                        .acceptStatus(AcceptStatus.PENDING) // 초기 상태는 PENDING
                        .build())
                .collect(Collectors.toList());

        // 4. Appointment에 참여자 목록 설정 (CascadeType.ALL 덕분에 함께 저장됨)
        appointment.getParticipants().addAll(appointmentParticipants);

        // 5. 약속 정보 저장
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // 6. 생성된 약속의 ID를 담아 응답 반환
        return new AppointmentResponseDto(savedAppointment.getId(), savedAppointment.getName());
    }
}