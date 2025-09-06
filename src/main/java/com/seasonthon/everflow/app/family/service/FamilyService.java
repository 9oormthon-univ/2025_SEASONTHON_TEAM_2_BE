package com.seasonthon.everflow.app.family.service;

import com.seasonthon.everflow.app.family.domain.Family;
import com.seasonthon.everflow.app.family.domain.FamilyJoinRequest;
import com.seasonthon.everflow.app.family.dto.FamilyCreateRequestDto;
import com.seasonthon.everflow.app.family.dto.FamilyInfoResponseDto;
import com.seasonthon.everflow.app.family.dto.FamilyJoinAnswerDto;
import com.seasonthon.everflow.app.family.dto.FamilyJoinRequestDto;
import com.seasonthon.everflow.app.family.dto.FamilyMembersResponseDto;
import com.seasonthon.everflow.app.family.dto.FamilyVerificationResponseDto;
import com.seasonthon.everflow.app.family.repository.FamilyJoinRequestRepository;
import com.seasonthon.everflow.app.family.repository.FamilyRepository;
import com.seasonthon.everflow.app.global.code.status.ErrorStatus;
import com.seasonthon.everflow.app.global.exception.GeneralException;
import com.seasonthon.everflow.app.notification.domain.NotificationType;
import com.seasonthon.everflow.app.notification.service.NotificationService;
import com.seasonthon.everflow.app.user.domain.RoleType;
import com.seasonthon.everflow.app.user.domain.User;
import com.seasonthon.everflow.app.user.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FamilyService {

    private static final int MAX_ATTEMPTS = 4;

    private final FamilyRepository familyRepository;
    private final FamilyJoinRequestRepository familyJoinRequestRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public void createFamily(Long userId, FamilyCreateRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        if (user.getRoleType() != RoleType.ROLE_GUEST) {
            throw new GeneralException(ErrorStatus.FAMILY_ALREADY_EXISTS);
        }

        user.updateNickname(request.getNickname());

        Family family = Family.builder()
                .familyName(request.getFamilyName())
                .verificationQuestion(request.getVerificationQuestion())
                .verificationAnswer(request.getVerificationAnswer())
                .build();

        family.addMember(user);
        user.updateRole(RoleType.ROLE_USER);

        familyRepository.save(family);
    }

    @Transactional
    public FamilyVerificationResponseDto getVerificationQuestion(Long userId, FamilyJoinRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        if (user.getRoleType() != RoleType.ROLE_GUEST) {
            throw new GeneralException(ErrorStatus.FAMILY_ALREADY_EXISTS);
        }

        user.updateNickname(request.getNickname());

        Family family = familyRepository.findByInviteCode(request.getInviteCode())
                .orElseThrow(() -> new GeneralException(ErrorStatus.FAMILY_NOT_FOUND));

        user.resetFamilyJoinAttempts();
        userRepository.save(user);

        return new FamilyVerificationResponseDto(family.getVerificationQuestion());
    }

    @Transactional(noRollbackFor = GeneralException.class)
    public void joinFamily(Long userId, FamilyJoinAnswerDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        if (user.getRoleType() != RoleType.ROLE_GUEST) {
            throw new GeneralException(ErrorStatus.FAMILY_ALREADY_EXISTS);
        }

        int attempts = user.getFamilyJoinAttempts() == null ? 0 : user.getFamilyJoinAttempts();
        if (attempts >= MAX_ATTEMPTS) {
            throw new GeneralException(ErrorStatus.FAMILY_JOIN_ATTEMPT_EXCEEDED);
        }

        Family family = familyRepository.findByInviteCode(request.getInviteCode())
                .orElseThrow(() -> new GeneralException(ErrorStatus.FAMILY_NOT_FOUND));

        List<User> existingMembers = userRepository.findAllByFamilyId(family.getId());

        if (!family.getVerificationAnswer().equals(request.getVerificationAnswer())) {
            user.increaseFamilyJoinAttempts();
            userRepository.save(user);

            if (user.getFamilyJoinAttempts() >= MAX_ATTEMPTS) {
                String link = "";
                String contentText = String.format(
                        "%s님이 가족 가입에 %d회 연속 실패했습니다. 가입 요청을 확인해주세요.",
                        user.getNickname(), MAX_ATTEMPTS
                );

                existingMembers.forEach(recipient -> notificationService.sendNotification(
                        recipient,
                        NotificationType.FAMILY_ACTION,
                        contentText,
                        link
                ));
                throw new GeneralException(ErrorStatus.FAMILY_JOIN_ATTEMPT_EXCEEDED);
            }
            throw new GeneralException(ErrorStatus.INVALID_VERIFICATION_ANSWER);
        }

        family.addMember(user);
        user.updateRole(RoleType.ROLE_USER);
        user.resetFamilyJoinAttempts();
        userRepository.save(user);

        String link = "";
        String contentText = String.format("%s님이 %s에 입장했어요.", user.getNickname(), family.getFamilyName());

        existingMembers.stream()
                .filter(member -> !member.getId().equals(user.getId()))
                .forEach(recipient -> notificationService.sendNotification(
                        recipient,
                        NotificationType.FAMILY_RESPONSE,
                        contentText,
                        link
                ));

        familyRepository.save(family);
    }

    @Transactional(readOnly = true)
    public FamilyInfoResponseDto getMyFamily(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        Family family = user.getFamily();
        if (family == null) {
            throw new GeneralException(ErrorStatus.NOT_IN_FAMILY_YET);
        }

        return new FamilyInfoResponseDto(
                family.getInviteCode(),
                family.getFamilyName(),
                family.getVerificationQuestion(),
                family.getVerificationAnswer()
        );
    }

    @Transactional(readOnly = true)
    public FamilyMembersResponseDto getFamilyMembers(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        Family family = user.getFamily();
        if (family == null) {
            throw new GeneralException(ErrorStatus.FAMILY_NOT_FOUND);
        }

        List<FamilyMembersResponseDto.MemberInfo> memberInfos = family.getMembers().stream()
                .sorted(Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(member -> new FamilyMembersResponseDto.MemberInfo(
                        member.getNickname(),
                        member.getProfileUrl()
                ))
                .toList();

        return new FamilyMembersResponseDto(family.getFamilyName(), memberInfos);
    }

    public void approveJoinRequest(Long approverId, Long requestId) {
        FamilyJoinRequest joinRequest = familyJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.REQUEST_NOT_FOUND));

        Family family = joinRequest.getFamily();

        User creator = family.getMembers().stream()
                .min(Comparator.comparing(User::getCreatedAt))
                .orElseThrow(() -> new GeneralException(ErrorStatus.FAMILY_NOT_FOUND));

        if (!creator.getId().equals(approverId)) {
            throw new GeneralException(ErrorStatus.FORBIDDEN);
        }

        User user = joinRequest.getUser();
        family.addMember(user);
        user.updateRole(RoleType.ROLE_USER);

        joinRequest.approve();

        familyJoinRequestRepository.save(joinRequest);
        userRepository.save(user);
        familyRepository.save(family);
    }

    public void rejectJoinRequest(Long approverId, Long requestId) {
        FamilyJoinRequest joinRequest = familyJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.REQUEST_NOT_FOUND));

        Family family = joinRequest.getFamily();

        User creator = family.getMembers().stream()
                .min(Comparator.comparing(User::getCreatedAt))
                .orElseThrow(() -> new GeneralException(ErrorStatus.FAMILY_NOT_FOUND));

        if (!creator.getId().equals(approverId)) {
            throw new GeneralException(ErrorStatus.FORBIDDEN);
        }

        joinRequest.reject();
        familyJoinRequestRepository.save(joinRequest);
    }
}
