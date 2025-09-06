package com.seasonthon.everflow.app.topic.service;

import com.seasonthon.everflow.app.gemini.service.GeminiService;
import com.seasonthon.everflow.app.global.code.status.ErrorStatus;
import com.seasonthon.everflow.app.global.exception.GeneralException;
import com.seasonthon.everflow.app.notification.domain.NotificationType;
import com.seasonthon.everflow.app.notification.service.NotificationService;
import com.seasonthon.everflow.app.topic.domain.Topic;
import com.seasonthon.everflow.app.topic.domain.TopicAnswer;
import com.seasonthon.everflow.app.topic.domain.TopicStatus;
import com.seasonthon.everflow.app.topic.domain.TopicType;
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
    private final GeminiService geminiService;

    // == 내부 유틸: 토픽 활성 여부 ==
    private boolean isActive(Topic t) {
        LocalDateTime now = LocalDateTime.now();
        return t.getStatus() == TopicStatus.ACTIVE
                && !t.getActiveFrom().isAfter(now)
                && t.getActiveUntil().isAfter(now);
    }

    // == [관리자] 새 토픽 등록 (기본 3일 활성) ==
    @Transactional
    public TopicResponse createTopic(TopicCreateRequest req) {
        LocalDateTime from = (req.activeFrom() != null) ? req.activeFrom() : LocalDateTime.now();
        LocalDateTime until = from.plusDays(3);

        TopicType type = (req.topicType() != null) ? req.topicType() : TopicType.CASUAL;

        Topic topic = Topic.builder()
                .question(req.question())
                .activeFrom(from)
                .activeUntil(until)
                .type(type)   // @Builder 생성자 파라미터명이 type
                .build();

        topic.activate();
        return TopicResponse.of(topicRepository.save(topic));
    }

    // == [관리자] 토픽 문구 수정 ==
    @Transactional
    public TopicResponse updateTopic(Long topicId, TopicUpdateRequest req) {
        Topic t = topicRepository.findById(topicId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.TOPIC_NOT_FOUND));
        t.updateQuestion(req.question());
        return TopicResponse.of(t);
    }

    // == 현재 활성 토픽 조회 (응답 직전 남은 일수 계산) ==
    public TopicResponse getCurrentActiveTopic() {
        LocalDateTime now = LocalDateTime.now();

        Topic t = topicRepository
                .findFirstByStatusAndActiveFromLessThanEqualAndActiveUntilGreaterThanOrderByActiveFromDesc(
                        TopicStatus.ACTIVE, now, now
                )
                .orElseThrow(() -> new GeneralException(ErrorStatus.TOPIC_NOT_FOUND));

        // 응답 직전 남은 일수 캐시 갱신(저장 X, 조회만 최신화)
        t.refreshRemainingDays();

        return TopicResponse.of(t);
    }

    // == [사용자] 활성 토픽에 내 답변 생성 ==
    // 이미 답변이 있으면 예외(수정 API 사용)
    @Transactional
    public AnswerResponse createAnswer(Long topicId, Long userId, AnswerCreateRequest req) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.TOPIC_NOT_FOUND));
        if (!isActive(topic)) {
            throw new GeneralException(ErrorStatus.TOPIC_NOT_ACTIVE);
        }

        // 중복 방지: 기존 답변 존재 시 예외 (정책 유지)
        answerRepository.findByTopicIdAndUserId(topicId, userId)
                .ifPresent(a -> { throw new GeneralException(ErrorStatus.ANSWER_ALREADY_EXISTS); });

        User user = userRepository.findById(userId).orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        TopicAnswer a = TopicAnswer.builder()
                .topic(topic)
                .user(user)
                .content(req.content())
                .build();

        // 가족 알림 발송 (본인 제외)
        if (user.getFamily() != null) {
            List<User> familyMembers = userRepository.findAllByFamilyId(user.getFamily().getId());
            String link = "/api/topics/" + topicId + "/answers/family";
            String contentText = String.format("%s님이 세대토픽에 답변했어요.", user.getNickname());

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

    // == [사용자] 활성 토픽에 내 답변 수정 ==
    @Transactional
    public AnswerResponse updateAnswer(Long topicId, Long userId, AnswerUpdateRequest req) {
        Topic t = topicRepository.findById(topicId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.TOPIC_NOT_FOUND));
        if (!isActive(t)) {
            throw new GeneralException(ErrorStatus.TOPIC_NOT_ACTIVE);
        }

        TopicAnswer a = answerRepository.findByTopicIdAndUserId(topicId, userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.ANSWER_NOT_FOUND));
        a.updateContent(req.content());
        return AnswerResponse.of(a);
    }

    // == 특정 토픽의 모든 답변(공개) ==
    public List<AnswerResponse> getTopicAnswers(Long topicId) {
        return answerRepository.findAllByTopicId(topicId)
                .stream()
                .map(AnswerResponse::of)
                .toList();
    }

    // == (활성 토픽) 가족 답변 목록 ==
    public List<AnswerResponse> getFamilyAnswers(Long familyId) {
        Topic current = topicRepository.findFirstByStatusAndActiveFromLessThanEqualAndActiveUntilGreaterThanOrderByActiveFromDesc(
                TopicStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now()
        ).orElseThrow(() -> new GeneralException(ErrorStatus.TOPIC_NOT_FOUND));

        return answerRepository.findFamilyAnswers(current.getId(), familyId)
                .stream()
                .map(AnswerResponse::of)
                .toList();
    }

    // == (특정 토픽) 가족 답변 목록 ==
    public List<AnswerResponse> getFamilyAnswersByTopic(Long topicId, Long familyId) {
        return answerRepository.findFamilyAnswersByTopic(topicId, familyId)
                .stream()
                .map(AnswerResponse::of)
                .toList();
    }

    // == 가족이 답변 남긴 토픽 목록 + 총 개수 ==
    public FamilyAnsweredTopicsResponse getFamilyAnsweredTopics(Long familyId) {
        List<TopicAnswer> raws = answerRepository.findAllByFamilyId(familyId);

        Map<Topic, List<TopicAnswer>> grouped = raws.stream()
                .collect(Collectors.groupingBy(
                        TopicAnswer::getTopic,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<TopicResponse> topics = grouped.keySet()
                .stream()
                .map(TopicResponse::of)
                .toList();

        return new FamilyAnsweredTopicsResponse(topics, topics.size());
    }

    // == [관리자/스케줄러] Gemini로 질문 생성하여 3일 활성 토픽 등록 ==
    @Transactional
    public TopicResponse createDailyTopicFromGemini(TopicType type) {
        // 1) 제미나이로 질문 생성
        String question = geminiService.generateDailyQuestion(type);

        // 2) 3일 활성 기간으로 저장
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime until = from.plusDays(3);

        Topic topic = Topic.builder()
                .question(question)   // ★ 여기 고침
                .activeFrom(from)
                .activeUntil(until)
                .type(type)           // @Builder 파라미터명이 type
                .build();

        return TopicResponse.of(topicRepository.save(topic));
    }
}