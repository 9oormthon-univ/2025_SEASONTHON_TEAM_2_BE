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

    private boolean isActive(Topic t) {
        LocalDateTime now = LocalDateTime.now();
        return t.getStatus() == TopicStatus.ACTIVE
                && !t.getActiveFrom().isAfter(now)
                && t.getActiveUntil().isAfter(now);
    }

    @Transactional
    public TopicResponse createTopic(TopicCreateRequest req) {
        LocalDateTime from = (req.activeFrom() != null) ? req.activeFrom() : LocalDateTime.now();
        LocalDateTime until = from.plusDays(3);

        TopicType type = (req.topicType() != null) ? req.topicType() : TopicType.CASUAL;

        Topic topic = Topic.builder()
                .question(req.question())
                .activeFrom(from)
                .activeUntil(until)
                .type(type)
                .build();

        topic.activate();
        return TopicResponse.of(topicRepository.save(topic));
    }

    @Transactional
    public TopicResponse updateTopic(Long topicId, TopicUpdateRequest req) {
        Topic t = topicRepository.findById(topicId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.TOPIC_NOT_FOUND));
        t.updateQuestion(req.question());
        return TopicResponse.of(t);
    }

    public TopicResponse getCurrentActiveTopic() {
        LocalDateTime now = LocalDateTime.now();

        Topic t = topicRepository
                .findFirstByStatusAndActiveFromLessThanEqualAndActiveUntilGreaterThanOrderByActiveFromDesc(
                        TopicStatus.ACTIVE, now, now
                )
                .orElseThrow(() -> new GeneralException(ErrorStatus.TOPIC_NOT_FOUND));

        t.refreshRemainingDays();

        return TopicResponse.of(t);
    }

    @Transactional
    public AnswerResponse createAnswer(Long topicId, Long userId, AnswerCreateRequest req) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.TOPIC_NOT_FOUND));
        if (!isActive(topic)) {
            throw new GeneralException(ErrorStatus.TOPIC_NOT_ACTIVE);
        }

        answerRepository.findByTopicIdAndUserId(topicId, userId)
                .ifPresent(a -> { throw new GeneralException(ErrorStatus.ANSWER_ALREADY_EXISTS); });

        User user = userRepository.findById(userId).orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        TopicAnswer a = TopicAnswer.builder()
                .topic(topic)
                .user(user)
                .content(req.content())
                .build();

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

    public List<AnswerResponse> getTopicAnswers(Long topicId) {
        return answerRepository.findAllByTopicId(topicId)
                .stream()
                .map(AnswerResponse::of)
                .toList();
    }

    public List<AnswerResponse> getFamilyAnswers(Long familyId) {
        Topic current = topicRepository.findFirstByStatusAndActiveFromLessThanEqualAndActiveUntilGreaterThanOrderByActiveFromDesc(
                TopicStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now()
        ).orElseThrow(() -> new GeneralException(ErrorStatus.TOPIC_NOT_FOUND));

        return answerRepository.findFamilyAnswers(current.getId(), familyId)
                .stream()
                .map(AnswerResponse::of)
                .toList();
    }

    public List<AnswerResponse> getFamilyAnswersByTopic(Long topicId, Long familyId) {
        return answerRepository.findFamilyAnswersByTopic(topicId, familyId)
                .stream()
                .map(AnswerResponse::of)
                .toList();
    }

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

    @Transactional
    public TopicResponse createDailyTopicFromGemini(TopicType type, List<String> recentQuestions) {

        String question = geminiService.generateDailyQuestion(type, recentQuestions);

        LocalDateTime from = LocalDateTime.now();
        LocalDateTime until = from.plusDays(3);

        Topic topic = Topic.builder()
                .question(question)
                .activeFrom(from)
                .activeUntil(until)
                .type(type)
                .build();

        return TopicResponse.of(topicRepository.save(topic));
    }
}