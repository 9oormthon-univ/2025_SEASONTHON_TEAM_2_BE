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

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository; // User 정보를 가져오기 위해 필요

    @Transactional
    public AppointmentResponseDto.AppointmentAddResponseDto addAppointment(AppointmentRequestDto requestDto, Long proposeUserId) {
        // 1. 약속을 제안한 사용자(proposeUser) 정보를 DB에서 조회
        User proposeUser = userRepository.findById(proposeUserId)
                .orElseThrow(() -> new IllegalArgumentException("제안자를 찾을 수 없습니다."));

        // 2. Appointment 엔터티 생성
        Appointment appointment = Appointment.builder()
                .proposeUser(proposeUser)
                .family(proposeUser.getFamily())
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
        return new AppointmentResponseDto.AppointmentAddResponseDto(savedAppointment.getId(), savedAppointment.getName());
    }

    public AppointmentResponseDto.AppointmentMonthResponseDto getMonthAppointment(Long familyId, int year, int month) {
        // 1. 조회할 월의 시작일과 종료일 계산
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        // 2. Repository를 통해 해당 기간의 약속 데이터를 조회
        List<Appointment> appointments = appointmentRepository.findAppointmentsOverlappingWithDateRange(familyId, startOfMonth, endOfMonth);

        // 3. flatMap을 사용하여 각 약속의 기간에 포함되는 모든 날짜를 추출
        List<String> daysWithAppointments = appointments.stream()
                .flatMap(appointment -> {
                    LocalDate startDate = appointment.getStartTime().toLocalDate();
                    LocalDate endDate = appointment.getEndTime().toLocalDate();

                    return startDate.datesUntil(endDate.plusDays(1));
                })
                .map(LocalDate::toString)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // 4. 결과를 DTO에 담아 반환
        return new AppointmentResponseDto.AppointmentMonthResponseDto(daysWithAppointments);
    }

    public AppointmentResponseDto.AppointmentDetailResponseDto getAppointment(Long appointmentId) {
        // 1. appointmentId를 사용하여 DB에서 약속 엔터티를 조회합니다.
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 약속을 찾을 수 없습니다. id=" + appointmentId));

        // 2. 날짜/시간(LocalDateTime)을 원하는 형식의 문자열(String)으로 변환하기 위한 포맷터를 정의합니다.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // 3. 조회된 엔터티의 정보를 사용하여 DTO를 생성하고 반환합니다.
        return new AppointmentResponseDto.AppointmentDetailResponseDto(
                appointment.getId(),
                appointment.getName(),
                appointment.getStartTime().format(formatter),
                appointment.getEndTime().format(formatter),
                appointment.getLocation(),
                appointment.getContent(),
                appointment.getProposeUser().getNickname()
        );
    }

    @Transactional
    public AppointmentResponseDto.MessageResponseDto deleteAppointment(Long appointmentId) {
        // 1. 삭제하려는 약속이 실제로 존재하는지 확인합니다. (존재하지 않으면 예외 발생)
        if (!appointmentRepository.existsById(appointmentId)) {
            throw new IllegalArgumentException("해당 약속을 찾을 수 없습니다. id=" + appointmentId);
        }

        // 2. JpaRepository가 기본으로 제공하는 deleteById 메서드를 사용하여 약속을 삭제합니다.
        appointmentRepository.deleteById(appointmentId);

        return new AppointmentResponseDto.MessageResponseDto("약속을 삭제하셨습니다.");
    }

}