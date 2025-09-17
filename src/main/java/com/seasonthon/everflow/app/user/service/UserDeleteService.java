package com.seasonthon.everflow.app.user.service;

import com.seasonthon.everflow.app.appointment.repository.AppointmentParticipantRepository;
import com.seasonthon.everflow.app.appointment.repository.AppointmentRepository;
import com.seasonthon.everflow.app.bookshelf.repository.BookshelfAnswerRepository;
import com.seasonthon.everflow.app.bookshelf.repository.BookshelfQuestionRepository;
import com.seasonthon.everflow.app.family.repository.FamilyJoinRequestRepository;
import com.seasonthon.everflow.app.global.code.dto.UserInfoResponseDto;
import com.seasonthon.everflow.app.global.code.status.ErrorStatus;
import com.seasonthon.everflow.app.global.exception.GeneralException;
import com.seasonthon.everflow.app.global.oauth.domain.CustomUserDetails;
import com.seasonthon.everflow.app.memo.repository.MemoRepository;
import com.seasonthon.everflow.app.notification.repository.EmitterRepository;
import com.seasonthon.everflow.app.notification.repository.NotificationRepository;
import com.seasonthon.everflow.app.topic.repository.TopicAnswerRepository;
import com.seasonthon.everflow.app.user.domain.User;
import com.seasonthon.everflow.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDeleteService {
    private final UserRepository userRepository;
    private final TopicAnswerRepository topicAnswerRepository;
    private final NotificationRepository notificationRepository;
    private final BookshelfAnswerRepository bookshelfAnswerRepository;
    private final BookshelfQuestionRepository bookshelfQuestionRepository;
    private final AppointmentParticipantRepository appointmentParticipantRepository;
    private final FamilyJoinRequestRepository familyJoinRequestRepository;
    private final MemoRepository memoRepository;
    private final EmitterRepository emitterRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    public UserInfoResponseDto getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        String familyCode = (user.getFamily() != null) ? user.getFamily().getInviteCode() : null;

        return new UserInfoResponseDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileUrl(),
                user.getRoleType().toString(),
                familyCode
        );
    }

    /**
     * 인증 객체에서 userId 반환 (미인증 예외)
     */
    @Transactional(readOnly = true)
    public Long getUserId(CustomUserDetails me) {
        if (me == null) {
            throw new GeneralException(ErrorStatus.AUTH_REQUIRED);
        }
        return me.getUserId();
    }

    @Transactional
    public void withdrawUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        bookshelfAnswerRepository.deleteAllByUserId(userId);
        topicAnswerRepository.deleteAllByUserId(userId);
        notificationRepository.deleteAllByUserId(userId);
        bookshelfQuestionRepository.deleteAllByCreatedById(userId);
        appointmentParticipantRepository.deleteAllByUserId(userId);
        familyJoinRequestRepository.deleteAllByUserId(userId);
        memoRepository.deleteAllByUpdatedBy(userId);
        emitterRepository.deleteAllByUserId(userId);
        appointmentRepository.deleteAllByProposeUser_Id(userId);

        user.setFamily(null);
        userRepository.delete(user);
    }
}
