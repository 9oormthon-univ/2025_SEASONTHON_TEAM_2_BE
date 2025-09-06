package com.seasonthon.everflow.app.appointment.service;

import com.seasonthon.everflow.app.appointment.domain.AcceptStatus;
import com.seasonthon.everflow.app.appointment.domain.Appointment;
import com.seasonthon.everflow.app.appointment.domain.AppointmentParticipant;
import com.seasonthon.everflow.app.appointment.dto.AppointmentRequestDto;
import com.seasonthon.everflow.app.appointment.dto.AppointmentResponseDto;
import com.seasonthon.everflow.app.appointment.repository.AppointmentParticipantRepository;
import com.seasonthon.everflow.app.appointment.repository.AppointmentRepository;
import com.seasonthon.everflow.app.global.code.status.ErrorStatus;
import com.seasonthon.everflow.app.global.oauth.domain.CustomUserDetails;
import com.seasonthon.everflow.app.global.oauth.service.AuthService;
import com.seasonthon.everflow.app.notification.domain.Notification;
import com.seasonthon.everflow.app.notification.domain.NotificationType;
import com.seasonthon.everflow.app.notification.repository.NotificationRepository;
import com.seasonthon.everflow.app.notification.service.NotificationService;
import com.seasonthon.everflow.app.user.domain.User;
import com.seasonthon.everflow.app.user.repository.UserRepository;
import com.seasonthon.everflow.app.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentParticipantRepository appointmentParticipantRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuthService authService;

    @Transactional
    public AppointmentResponseDto.AppointmentAddResponseDto addAppointment(AppointmentRequestDto.AppointmentAddRequestDto requestDto, Long proposeUserId) {
        // 1. 약속을 제안한 사용자(proposeUser) 정보를 DB에서 조회
        User proposeUser = userRepository.findById(proposeUserId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

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
            throw new GeneralException(ErrorStatus.USER_NOT_FOUND);
        }

        List<AppointmentParticipant> appointmentParticipants = participants.stream()
                .map(participantUser -> AppointmentParticipant.builder()
                        .appointment(appointment)
                        .user(participantUser)
                        .acceptStatus(AcceptStatus.PENDING)
                        .build())
                .collect(Collectors.toList());

        // 4. Appointment에 참여자 목록 설정 (CascadeType.ALL 덕분에 함께 저장됨)
        appointment.getParticipants().addAll(appointmentParticipants);

        // 5. 약속 정보 저장
        Appointment savedAppointment = appointmentRepository.save(appointment);

        String link = "/api/appointments/" + savedAppointment.getId();
        participants.forEach(participantUser -> {
            String contentText = String.format("%s님이 %s에게 약속을 신청했어요.", proposeUser.getNickname(), participantUser.getNickname());
            notificationService.sendNotification(participantUser, NotificationType.APPOINTMENT_ACTION, contentText, link);
        });

        // 6. 생성된 약속의 ID를 담아 응답 반환
        return new AppointmentResponseDto.AppointmentAddResponseDto(savedAppointment.getId(), savedAppointment.getName());
    }

    public AppointmentResponseDto.AppointmentMonthResponseDto getMonthAppointment(CustomUserDetails userDetails, int year, int month) {
        Long familyId = authService.getFamilyId(userDetails);

        if (month < 1 || month > 12) {
            throw new GeneralException(ErrorStatus.INVALID_MONTH_PARAMETER);
        }

        // 1. 조회할 월의 시작일과 종료일 계산
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        LocalDateTime startOfMonthWithTime = startOfMonth.atStartOfDay(); // 00:00:00
        LocalDateTime endOfMonthWithTime = endOfMonth.atTime(23, 59, 59); // 23:59:59

        // 2. Repository를 통해 해당 기간의 약속 데이터를 조회
        List<Appointment> appointments = appointmentRepository.findAppointmentsOverlappingWithDateRange(familyId, startOfMonthWithTime, endOfMonthWithTime);

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

    public List<AppointmentResponseDto.AppointmentDateResponseDto> getDateAppointment(CustomUserDetails userDetails,  int year, int month, int day) {
        Long familyId = authService.getFamilyId(userDetails);

        if (month < 1 || month > 12) {
            throw new GeneralException(ErrorStatus.INVALID_MONTH_PARAMETER);
        }
        YearMonth yearMonth = YearMonth.of(year, month);
        if (day < 1 || day > yearMonth.lengthOfMonth()) {
            throw new GeneralException(ErrorStatus.INVALID_DAY_PARAMETER);
        }

        // 1. 요청된 날짜의 시작 시간과 종료 시간을 LocalDateTime으로 정의합니다.
        LocalDate targetDate = LocalDate.of(year, month, day);
        LocalDateTime startOfDay = targetDate.atStartOfDay(); // 예: 2025-09-04T00:00:00
        LocalDateTime endOfDay = targetDate.atTime(23, 59, 59);   // 예: 2025-09-04T23:59:59

        // 2. 기존 Repository 메서드를 재사용하여 해당 날짜와 겹치는 모든 약속을 조회합니다.
        List<Appointment> appointmentsOnDate = appointmentRepository.findAppointmentsOverlappingWithDateRange(familyId, startOfDay, endOfDay);

        // 3. 조회된 Appointment 엔터티 리스트를 AppointmentDateResponseDto 리스트로 변환합니다.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        return appointmentsOnDate.stream()
                .map(appointment -> new AppointmentResponseDto.AppointmentDateResponseDto(
                        appointment.getId(),
                        appointment.getName(),
                        appointment.getStartTime().format(formatter),
                        appointment.getEndTime().format(formatter),
                        appointment.getLocation(),
                        appointment.getProposeUser().getNickname(),
                        (long) appointment.getParticipants().size()-1,
                        appointment.getColor()
                ))
                .collect(Collectors.toList());
    }

    public AppointmentResponseDto.AppointmentDetailResponseDto getAppointment(Long appointmentId) {
        // 1. appointmentId를 사용하여 DB에서 약속 엔터티를 조회합니다.
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.APPOINTMENT_NOT_FOUND));

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
                appointment.getProposeUser().getNickname(),
                appointment.getColor()
        );
    }

    @Transactional
    public AppointmentResponseDto.MessageResponseDto deleteAppointment(Long appointmentId) {
        // 1. 삭제하려는 약속이 실제로 존재하는지 확인합니다. (존재하지 않으면 예외 발생)
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.APPOINTMENT_NOT_FOUND));

        // 2. JpaRepository가 기본으로 제공하는 delete 메서드를 사용하여 약속을 삭제합니다.
        appointmentRepository.delete(appointment);

        return new AppointmentResponseDto.MessageResponseDto("약속을 삭제하셨습니다.");
    }

    @Transactional
    public AppointmentResponseDto.MessageResponseDto updateParticipantStatus(Long appointmentId, Long userId, AcceptStatus newStatus) {
        AppointmentParticipant participant = appointmentParticipantRepository.findByAppointmentIdAndUserId(appointmentId, userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PARTICIPANT_NOT_FOUND));

        participant.updateStatus(newStatus);

        User proposer = participant.getAppointment().getProposeUser();
        User participantUser = participant.getUser();

        // 3-1. 응답 상태에 따라 알림 내용 결정
        String contentText = "";
        if (newStatus == AcceptStatus.ACCEPTED) {
            contentText = String.format("%s님이 약속을 수락했어요.", participantUser.getNickname());
        } else if (newStatus == AcceptStatus.REJECTED) {
            contentText = String.format("%s님이 약속을 거절했어요.", participantUser.getNickname());
        }

        // 3-2. 알림 엔터티 생성 및 저장
        if (!contentText.isEmpty()) {
            String link = "/api/appointments/" + appointmentId;
            notificationService.sendNotification(proposer, NotificationType.APPOINTMENT_RESPONSE, contentText, link);
        }

        return new AppointmentResponseDto.MessageResponseDto("참여 상태가 성공적으로 변경되었습니다.");
    }

}