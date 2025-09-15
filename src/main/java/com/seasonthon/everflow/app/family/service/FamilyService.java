package com.seasonthon.everflow.app.family.service;

import com.seasonthon.everflow.app.family.domain.Family;
import com.seasonthon.everflow.app.family.domain.FamilyJoinRequest;
import com.seasonthon.everflow.app.family.domain.JoinStatus;
import com.seasonthon.everflow.app.family.dto.FamilyCreateRequestDto;
import com.seasonthon.everflow.app.family.dto.FamilyEditRequestDto;
import com.seasonthon.everflow.app.family.dto.FamilyInfoResponseDto;
import com.seasonthon.everflow.app.family.dto.FamilyJoinAnswerDto;
import com.seasonthon.everflow.app.family.dto.FamilyJoinRequestDto;
import com.seasonthon.everflow.app.family.dto.FamilyMembersResponseDto;
import com.seasonthon.everflow.app.family.dto.FamilyVerificationDetailResponseDto;
import com.seasonthon.everflow.app.family.dto.FamilyVerificationResponseDto;
import com.seasonthon.everflow.app.family.dto.JoinAttemptResponseDto;
import com.seasonthon.everflow.app.family.dto.PendingJoinRequestDto;
import com.seasonthon.everflow.app.family.repository.FamilyJoinRequestRepository;
import com.seasonthon.everflow.app.family.repository.FamilyRepository;
import com.seasonthon.everflow.app.global.code.status.ErrorStatus;
import com.seasonthon.everflow.app.global.code.status.SuccessStatus;
import com.seasonthon.everflow.app.global.exception.GeneralException;
import com.seasonthon.everflow.app.notification.domain.NotificationType;
import com.seasonthon.everflow.app.notification.domain.ReadStatus;
import com.seasonthon.everflow.app.notification.repository.NotificationRepository;
import com.seasonthon.everflow.app.notification.service.NotificationService;
import com.seasonthon.everflow.app.user.domain.RoleType;
import com.seasonthon.everflow.app.user.domain.User;
import com.seasonthon.everflow.app.user.repository.UserRepository;
import com.seasonthon.everflow.app.memo.domain.Memo;
import com.seasonthon.everflow.app.memo.repository.MemoRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
    private final NotificationRepository notificationRepository;
    private final MemoRepository memoRepository;

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
        /* 가족 구성원 추가 및 역할 업데이트 */
        family.addMember(user);
        user.updateRole(RoleType.ROLE_USER);

        /* 가족 먼저 저장 (ID 확보) */
        Family savedFamily = familyRepository.save(family);

        /* 가족 생성 시 공유 메모 자동 생성 (가족당 1장) */
        if (!memoRepository.existsByFamilyId(savedFamily.getId())) {
            memoRepository.save(Memo.create(savedFamily.getId(), user.getId()));
        }
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
        familyJoinRequestRepository.findByFamilyIdAndUserId(family.getId(), user.getId())
                .filter(r -> r.getStatus() == JoinStatus.PENDING)
                .ifPresent(familyJoinRequestRepository::delete);
        return new FamilyVerificationResponseDto(family.getVerificationQuestion());
    }

    @Transactional(noRollbackFor = GeneralException.class)
    public JoinAttemptResponseDto joinFamily(Long userId, FamilyJoinAnswerDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        if (user.getRoleType() != RoleType.ROLE_GUEST) {
            throw new GeneralException(ErrorStatus.FAMILY_ALREADY_EXISTS);
        }
        Family family = familyRepository.findByInviteCode(request.getInviteCode())
                .orElseThrow(() -> new GeneralException(ErrorStatus.FAMILY_NOT_FOUND));
        int attempts = user.getFamilyJoinAttempts() == null ? 0 : user.getFamilyJoinAttempts();
        if (attempts >= MAX_ATTEMPTS) {
            upsertPendingJoinRequest(family, user, attempts + 1);
            notifyFamilyAction(family, user);
            return new JoinAttemptResponseDto(false, true, ErrorStatus.FAMILY_JOIN_ATTEMPT_EXCEEDED);
        }
        boolean correct = family.getVerificationAnswer().equals(request.getVerificationAnswer());
        if (!correct) {
            user.increaseFamilyJoinAttempts();
            userRepository.save(user);
            if (user.getFamilyJoinAttempts() >= MAX_ATTEMPTS) {
                upsertPendingJoinRequest(family, user, user.getFamilyJoinAttempts());
                notifyFamilyAction(family, user);
                return new JoinAttemptResponseDto(false, true, ErrorStatus.FAMILY_JOIN_ATTEMPT_EXCEEDED);
            }
            return new JoinAttemptResponseDto(false, false, ErrorStatus.INVALID_VERIFICATION_ANSWER);
        }
        /* 기존 가족(레거시)에 메모가 없을 경우 보강 생성 */
        family.addMember(user);
        user.updateRole(RoleType.ROLE_USER);
        user.resetFamilyJoinAttempts();
        userRepository.save(user);
        familyRepository.save(family);
        /* 기존 가족(레거시)에 메모가 없을 경우 보강 생성 */
        if (!memoRepository.existsByFamilyId(family.getId())) {
            memoRepository.save(Memo.create(family.getId(), user.getId()));
        }
        notifyFamilyResponse(family, user);
        return new JoinAttemptResponseDto(true, false, SuccessStatus.OK);
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

        boolean isCreator = family.getMembers().stream()
                .min(
                        java.util.Comparator
                                .comparing(User::getCreatedAt, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))
                                .thenComparing(User::getId)
                )
                .map(u -> u.getId().equals(user.getId()))
                .orElse(false);

        List<FamilyMembersResponseDto.MemberInfo> memberInfos = family.getMembers().stream()
                .sorted(
                        java.util.Comparator.comparing(
                                User::getCreatedAt,
                                java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())
                        )
                )
                .map(member -> new FamilyMembersResponseDto.MemberInfo(
                        member.getNickname(),
                        member.getProfileUrl(),
                        member.getId()   // email 대신 Long id 반환
                ))
                .toList();

        return new FamilyMembersResponseDto(family.getFamilyName(), isCreator, memberInfos);
    }

    @Transactional(readOnly = true)
    public List<PendingJoinRequestDto> getPendingJoinRequests(Long approverId) {
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        Family family = approver.getFamily();
        if (family == null) {
            throw new GeneralException(ErrorStatus.NOT_IN_FAMILY_YET);
        }
        var creator = family.getMembers().stream()
                .min(java.util.Comparator.comparing(User::getCreatedAt))
                .orElseThrow(() -> new GeneralException(ErrorStatus.FAMILY_NOT_FOUND));
        if (!creator.getId().equals(approverId)) {
            throw new GeneralException(ErrorStatus.FORBIDDEN);
        }
        return familyJoinRequestRepository.findAllByFamilyIdAndStatus(family.getId(), JoinStatus.PENDING)
                .stream()
                .map(req -> new PendingJoinRequestDto(
                        req.getId(),
                        req.getUser().getId(),
                        req.getUser().getNickname(),
                        req.getAttempts()
                ))
                .toList();
    }

    public void approveJoinRequest(Long approverId, Long requestId) {
        FamilyJoinRequest joinRequest = familyJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.REQUEST_NOT_FOUND));

        notificationRepository.findAllByUserIdAndReadStatusOrderByCreatedAtDesc(approverId, ReadStatus.UNREAD).stream()
                .filter(n -> n.getNotificationType() == NotificationType.FAMILY_ACTION)
                .filter(n -> n.getContentText().contains(joinRequest.getUser().getNickname()))
                .findFirst()
                .ifPresent(notification -> {
                    notification.markAsRead();
                    notificationRepository.save(notification);
                });

        Family family = joinRequest.getFamily();
        var creator = family.getMembers().stream()
                .min(java.util.Comparator.comparing(User::getCreatedAt))
                .orElseThrow(() -> new GeneralException(ErrorStatus.FAMILY_NOT_FOUND));
        if (!creator.getId().equals(approverId)) {
            throw new GeneralException(ErrorStatus.FORBIDDEN);
        }
        if (joinRequest.getStatus() != JoinStatus.PENDING) {
            throw new GeneralException(ErrorStatus.REQUEST_NOT_FOUND);
        }
        User user = joinRequest.getUser();
        /* 메모 기능 도입 이전에 생성된 가족의 경우 보강 생성 */
        family.addMember(user);
        user.updateRole(RoleType.ROLE_USER);
        joinRequest.approve();
        familyJoinRequestRepository.save(joinRequest);
        userRepository.save(user);
        familyRepository.save(family);
        /* 메모 기능 도입 이전에 생성된 가족의 경우 보강 생성 */
        if (!memoRepository.existsByFamilyId(family.getId())) {
            memoRepository.save(Memo.create(family.getId(), user.getId()));
        }
    }

    public void rejectJoinRequest(Long approverId, Long requestId) {
        FamilyJoinRequest joinRequest = familyJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.REQUEST_NOT_FOUND));

        notificationRepository.findAllByUserIdAndReadStatusOrderByCreatedAtDesc(approverId, ReadStatus.UNREAD).stream()
                .filter(n -> n.getNotificationType() == NotificationType.FAMILY_ACTION)
                .filter(n -> n.getContentText().contains(joinRequest.getUser().getNickname()))
                .findFirst()
                .ifPresent(notification -> {
                    notification.markAsRead();
                    notificationRepository.save(notification);
                });

        Family family = joinRequest.getFamily();
        var creator = family.getMembers().stream()
                .min(java.util.Comparator.comparing(User::getCreatedAt))
                .orElseThrow(() -> new GeneralException(ErrorStatus.FAMILY_NOT_FOUND));
        if (!creator.getId().equals(approverId)) {
            throw new GeneralException(ErrorStatus.FORBIDDEN);
        }
        if (joinRequest.getStatus() != JoinStatus.PENDING) {
            throw new GeneralException(ErrorStatus.REQUEST_NOT_FOUND);
        }
        joinRequest.reject();
        familyJoinRequestRepository.save(joinRequest);
    }

    @Transactional
    public FamilyInfoResponseDto editFamilyProfile(Long userId, FamilyEditRequestDto req) {
        if (req == null || req.isAllEmpty()) {
            throw new GeneralException(ErrorStatus.BAD_REQUEST);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        Family family = user.getFamily();
        if (family == null) {
            throw new GeneralException(ErrorStatus.NOT_IN_FAMILY_YET);
        }
        User leader = family.getMembers().stream()
                .min(java.util.Comparator
                        .comparing(User::getCreatedAt, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))
                        .thenComparing(User::getId))
                .orElseThrow(() -> new GeneralException(ErrorStatus.FAMILY_NOT_FOUND));
        if (!leader.getId().equals(userId)) {
            throw new GeneralException(ErrorStatus.FORBIDDEN);
        }
        if (req.getFamilyName() != null && !req.getFamilyName().isBlank()) {
            family.updateFamilyName(req.getFamilyName().trim());
        }
        boolean qProvided = req.getVerificationQuestion() != null && !req.getVerificationQuestion().isBlank();
        boolean aProvided = req.getVerificationAnswer() != null && !req.getVerificationAnswer().isBlank();
        if (qProvided && aProvided) {
            family.updateVerification(req.getVerificationQuestion().trim(), req.getVerificationAnswer().trim());
        } else if (qProvided) {
            family.updateVerification(req.getVerificationQuestion().trim(), family.getVerificationAnswer());
        } else if (aProvided) {
            family.updateVerification(family.getVerificationQuestion(), req.getVerificationAnswer().trim());
        }
        familyRepository.save(family);
        return new FamilyInfoResponseDto(
                family.getInviteCode(),
                family.getFamilyName(),
                family.getVerificationQuestion(),
                family.getVerificationAnswer()
        );
    }

    private void upsertPendingJoinRequest(Family family, User user, int targetAttempts) {
        FamilyJoinRequest jr = familyJoinRequestRepository
                .findByFamilyIdAndUserId(family.getId(), user.getId())
                .orElseGet(() -> FamilyJoinRequest.builder().family(family).user(user).build());
        jr.markPending();
        while (jr.getAttempts() < targetAttempts) {
            jr.increaseAttempts();
        }
        familyJoinRequestRepository.save(jr);
    }

    private void notifyFamilyAction(Family family, User actor) {
        List<User> members = userRepository.findAllByFamilyId(family.getId());
        String link = "";
        String contentText = String.format("%s님이 가족 가입에 %d회 연속 실패했습니다. 가입 요청을 확인해주세요.", actor.getNickname(), MAX_ATTEMPTS);
        members.forEach(recipient -> notificationService.sendNotification(
                recipient, NotificationType.FAMILY_ACTION, contentText, link
        ));
    }

    private void notifyFamilyResponse(Family family, User newMember) {
        List<User> members = userRepository.findAllByFamilyId(family.getId());
        String link = "";
        String contentText = String.format("%s님이 %s에 입장했어요.", newMember.getNickname(), family.getFamilyName());
        members.stream()
                .filter(m -> !m.getId().equals(newMember.getId()))
                .forEach(recipient -> notificationService.sendNotification(
                        recipient, NotificationType.FAMILY_RESPONSE, contentText, link
                ));
    }

    @Transactional(readOnly = true)
    public boolean doesFamilyExistByCode(String inviteCode) {
        return familyRepository.existsByInviteCode(inviteCode);
    }

    @Transactional(readOnly = true)
    public Optional<FamilyVerificationDetailResponseDto> getFamilyDetailByCode(String inviteCode) {
        return familyRepository.findByInviteCode(inviteCode).map(family -> {
            List<User> members = userRepository.findAllByFamilyId(family.getId());

            User leader = members.stream()
                    .min(Comparator
                            .comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(User::getId))
                    .orElse(null);

            if (leader == null) return null;

            List<String> profileUrls = members.stream()
                    .map(User::getProfileUrl)
                    .toList();

            return new FamilyVerificationDetailResponseDto(
                    family.getFamilyName(),
                    leader.getNickname(),
                    profileUrls,
                    members.size()
            );
        });
    }
}
