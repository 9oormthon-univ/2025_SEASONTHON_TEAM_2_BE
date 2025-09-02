package com.seasonthon.everflow.app.appointment.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AppointmentRequestDto {

    @NotBlank(message = "약속 이름은 필수입니다.")
    private String name;

    private String content;

    private String location;

    @NotNull(message = "시작 날짜는 필수입니다.")
    @FutureOrPresent(message = "시작 날짜는 현재이거나 미래여야 합니다.")
    private LocalDate startTime;

    @NotNull(message = "종료 날짜는 필수입니다.")
    @FutureOrPresent(message = "종료 날짜는 현재이거나 미래여야 합니다.")
    private LocalDate endTime;

    private String color;

    @NotEmpty(message = "참여자는 최소 1명 이상이어야 합니다.")
    private List<Long> participantUserIds; // 초대할 참여자들의 user_id 목록
}