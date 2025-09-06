package com.seasonthon.everflow.app.topic.service;

import com.seasonthon.everflow.app.global.code.status.ErrorStatus;
import com.seasonthon.everflow.app.global.exception.GeneralException;
import com.seasonthon.everflow.app.notification.domain.NotificationType;
import com.seasonthon.everflow.app.notification.service.NotificationService;
import com.seasonthon.everflow.app.topic.domain.*;
import com.seasonthon.everflow.app.topic.dto.TopicDto.*;
import com.seasonthon.everflow.app.topic.repository.TopicAnswerRepository;
import com.seasonthon.everflow.app.topic.repository.TopicRepository;
import com.seasonthon.everflow.app.user.domain.User;
import com.seasonthon.everflow.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TopicService {

    private final TopicRepository topicRepository;
    private final TopicAnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // --- 내부 유틸 ---
    private boolean isActive(Topic t) {
        LocalDateTime now = LocalDateTime.now();
        return t.getStatus() == TopicStatus.ACTIVE
                && !t.getActiveFrom().isAfter(now)
                && t.getActiveUntil().isAfter(now);
    }

    /**
     * [관리자 전용]
     * 새 토픽을 등록한다. (기본 활성 기간: 3일)
     * status = ACTIVE 로 설정됨.
     */
    @Transactional
    public TopicResponse createTopic(TopicCreateRequest req) {
        LocalDateTime from = (req.activeFrom() != null) ? req.activeFrom() : LocalDateTime.now();
        LocalDateTime until = from.plusDays(3);

        Topic topic = Topic.builder()
                .question(req.question())
                .activeFrom(from)
                .activeUntil(until)
                .build();

        topic.activate();
        return TopicResponse.of(topicRepository.save(topic));
    }

    /**
     * [관리자 전용]
     * 특정 토픽의 질문 문구를 수정한다.
     */
    @Transactional
    public TopicResponse updateTopic(Long topicId, TopicUpdateRequest req) {
        Topic t = topicRepository.findById(topicId).orElseThrow(() -> new GeneralException(ErrorStatus.TOPIC_NOT_FOUND));
        t.updateQuestion(req.question());
        return TopicResponse.of(t);
    }

    /**
     * 현재 시점에서 활성화된 토픽 1개를 조회한다.
     */
    public TopicResponse getCurrentActiveTopic() {
        Topic t = topicRepository.findFirstByStatusAndActiveFromLessThanEqualAndActiveUntilGreaterThanOrderByActiveFromDesc(
                TopicStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now()
        ).orElseThrow(() -> new GeneralException(ErrorStatus.TOPIC_NOT_FOUND));
        return TopicResponse.of(t);
    }

    /**
     * [사용자 전용]
     * 활성화된 토픽에 대해 내 답변을 새로 작성한다.
     * - 이미 답변이 있으면 예외 발생 (수정 API 사용해야 함).
     */
    @Transactional
    public AnswerResponse createAnswer(Long topicId, Long userId, AnswerCreateRequest req) {
        Topic topic = topicRepository.findById(topicId).orElseThrow(() -> new GeneralException(ErrorStatus.TOPIC_NOT_FOUND));
        if (!isActive(topic)) {
            throw new GeneralException(ErrorStatus.TOPIC_NOT_ACTIVE);
        }

        answerRepository.findByTopicIdAndUserId(topicId, userId)
                .ifPresent(a -> { throw new IllegalStateException("이미 답변이 존재합니다. 수정 API를 사용하세요."); });

        User user = userRepository.findById(userId).orElseThrow();
        TopicAnswer a = TopicAnswer.builder()
                .topic(topic)
                .user(user)
                .content(req.content())
                .build();

        if (user.getFamily() != null) {
            // 3. 답변한 사용자의 가족 구성원 전체를 조회합니다.
            List<User> familyMembers = userRepository.findAllByFamilyId(user.getFamily().getId());

            // 4. 알림에 사용할 공통 링크와 내용을 준비합니다.
            String link = "/api/topics/" + topicId + "/answers/family";
            String contentText = String.format("%s님이 세대토픽에 답변했어요.", user.getNickname());

            // 5. 가족 구성원들에게 알림을 발송합니다 (답변자 본인 제외).
            familyMembers.stream()
                    .filter(member -> !member.getId().equals(userId))
                    .forEach(recipient -> notificationService.sendNotification(
                            recipient,
                            NotificationType.ANSWER_RESPONSE,
                            contentText,
                            link
                    ));
        }

        return AnswerResponse.of(answerRepository.save(a));
    }

    /**
     * [사용자 전용]
     * 활성화된 토픽에 대해 내 답변을 수정한다.
     */
    @Transactional
    public AnswerResponse updateAnswer(Long topicId, Long userId, AnswerUpdateRequest req) {
        Topic t = topicRepository.findById(topicId).orElseThrow(() -> new GeneralException(ErrorStatus.TOPIC_NOT_FOUND));
        if (!isActive(t)) {
            throw new GeneralException(ErrorStatus.TOPIC_NOT_ACTIVE);
        }

        TopicAnswer a = answerRepository.findByTopicIdAndUserId(topicId, userId).orElseThrow(() -> new GeneralException(ErrorStatus.ANSWER_NOT_FOUND));
        a.updateContent(req.content());
        return AnswerResponse.of(a);
    }

    /**
     * 특정 토픽에 달린 모든 답변을 조회한다. (전체 공개)
     */
    public List<AnswerResponse> getTopicAnswers(Long topicId) {
        return answerRepository.findAllByTopicId(topicId)
                .stream().map(AnswerResponse::of).toList();
    }

    /**
     * 현재 활성화된 토픽에 대해,
     * 특정 가족(familyId)의 모든 답변을 조회한다.
     */
    public List<AnswerResponse> getFamilyAnswers(Long familyId) {
        Topic current = topicRepository.findFirstByStatusAndActiveFromLessThanEqualAndActiveUntilGreaterThanOrderByActiveFromDesc(
                TopicStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now()
        ).orElseThrow(() -> new GeneralException(ErrorStatus.TOPIC_NOT_FOUND));
        return answerRepository.findFamilyAnswers(current.getId(), familyId)
                .stream().map(AnswerResponse::of).toList();
    }

    /**
     * 특정 토픽에 대해,
     * 특정 가족(familyId)의 모든 답변을 조회한다.
     */
    public List<AnswerResponse> getFamilyAnswersByTopic(Long topicId, Long familyId) {
        return answerRepository.findFamilyAnswersByTopic(topicId, familyId)
                .stream().map(AnswerResponse::of).toList();
    }

    /**
     * 특정 가족(familyId)이 답변을 남긴 모든 토픽 목록을 조회한다.
     * - 토픽별 중복 제거 (첫 답변 순서 기준)
     * - 총 개수 포함
     */
    public FamilyAnsweredTopicsResponse getFamilyAnsweredTopics(Long familyId) {
        List<TopicAnswer> raws = answerRepository.findAllByFamilyId(familyId);

        Map<Topic, List<TopicAnswer>> grouped = raws.stream()
                .collect(Collectors.groupingBy(TopicAnswer::getTopic, LinkedHashMap::new, Collectors.toList()));

        List<TopicResponse> topics = grouped.keySet().stream().map(TopicResponse::of).toList();
        return new FamilyAnsweredTopicsResponse(topics, topics.size());
    }
}