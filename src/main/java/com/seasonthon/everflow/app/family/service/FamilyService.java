package com.seasonthon.everflow.app.family.service;

import com.seasonthon.everflow.app.family.domain.Family;
import com.seasonthon.everflow.app.family.dto.FamilyCreateRequestDto;
import com.seasonthon.everflow.app.family.dto.FamilyInfoResponseDto;
import com.seasonthon.everflow.app.family.dto.FamilyJoinAnswerDto;
import com.seasonthon.everflow.app.family.dto.FamilyJoinRequestDto;
import com.seasonthon.everflow.app.family.dto.FamilyMembersResponseDto;
import com.seasonthon.everflow.app.family.dto.FamilyVerificationResponseDto;
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

    private final FamilyRepository familyRepository;
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

    @Transactional(readOnly = true)
    public FamilyVerificationResponseDto getVerificationQuestion(Long userId, FamilyJoinRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        if (user.getRoleType() != RoleType.ROLE_GUEST) {
            throw new GeneralException(ErrorStatus.FAMILY_ALREADY_EXISTS);
        }

        user.updateNickname(request.getNickname());

        Family family = familyRepository.findByInviteCode(request.getInviteCode())
                .orElseThrow(() -> new GeneralException(ErrorStatus.FAMILY_NOT_FOUND));

        // 닉네임 업데이트는 가입 완료 단계에서 하는 것이 더 좋으므로, 여기서는 닉네임만 유효성 검사
        // user.updateNickname(request.getNickname());

        return new FamilyVerificationResponseDto(family.getVerificationQuestion());
    }

    public void joinFamily(Long userId, FamilyJoinAnswerDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));


        if (user.getRoleType() != RoleType.ROLE_GUEST) {
            throw new GeneralException(ErrorStatus.FAMILY_ALREADY_EXISTS);
        }

        Family family = familyRepository.findByInviteCode(request.getInviteCode())
                .orElseThrow(() -> new GeneralException(ErrorStatus.FAMILY_NOT_FOUND));

        List<User> existingMembers = userRepository.findAllByFamilyId(family.getId());

        if (!family.getVerificationAnswer().equals(request.getVerificationAnswer())) {
            user.increaseFamilyJoinAttempts();
            userRepository.save(user);

            if (user.getFamilyJoinAttempts() >= 3) {
                // TODO: 3회 이상 실패 시, 가입 대기 상태로 전환하고 알림을 보내는 등의 로직 추가
                String link = "";
                String contentText = String.format("%s님이 " + user.getFamily().getFamilyName()+ "에 참여를 요청했습니다.", user.getNickname());

                existingMembers.forEach(recipient -> notificationService.sendNotification(
                        recipient,
                        NotificationType.FAMILY_ACTION,
                        contentText,
                        link
                ));

                throw new GeneralException(ErrorStatus.FAMILY_JOIN_FAILED);
            }
            throw new GeneralException(ErrorStatus.INVALID_VERIFICATION_ANSWER);
        }

        family.addMember(user);
        user.updateRole(RoleType.ROLE_USER);
        user.resetFamilyJoinAttempts();

        String link = "";
        String contentText = String.format("%s님이 " + user.getFamily().getFamilyName()+ "에 입장했어요.", user.getNickname());

        // 5. 가족 구성원들에게 알림을 발송합니다 (답변자 본인 제외).
        existingMembers.forEach(recipient -> notificationService.sendNotification(
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
}
