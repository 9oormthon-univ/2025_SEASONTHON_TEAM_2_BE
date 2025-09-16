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
import com.seasonthon.everflow.app.topic.dto.*;
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
    public TopicResponseDto.Simple createTopic(TopicRequestDto.Create req) {
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
        return TopicResponseDto.Simple.of(topicRepository.save(topic));
    }

    @Transactional
    public TopicResponseDto.Simple updateTopic(Long topicId, TopicRequestDto.Update req) {
        Topic t = topicRepository.findById(topicId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.TOPIC_NOT_FOUND));
        t.updateQuestion(req.question());
        return TopicResponseDto.Simple.of(t);
    }

    public TopicResponseDto.Simple getCurrentActiveTopic() {
        LocalDateTime now = LocalDateTime.now();

        Topic t = topicRepository
                .findFirstByStatusAndActiveFromLessThanEqualAndActiveUntilGreaterThanOrderByActiveFromDesc(
                        TopicStatus.ACTIVE, now, now
                )
                .orElseThrow(() -> new GeneralException(ErrorStatus.TOPIC_NOT_FOUND));

        t.refreshRemainingDays();

        return TopicResponseDto.Simple.of(t);
    }

    @Transactional
    public TopicAnswerResponseDto.Info createAnswer(Long topicId, Long userId, TopicAnswerRequestDto.Create req) {
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
                .familyId(user.getFamily().getId())
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

        return TopicAnswerResponseDto.Info.of(answerRepository.save(a));
    }

    @Transactional
    public TopicAnswerResponseDto.Info updateAnswer(Long topicId, Long userId, TopicAnswerRequestDto.Update req) {
        Topic t = topicRepository.findById(topicId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.TOPIC_NOT_FOUND));
        if (!isActive(t)) {
            throw new GeneralException(ErrorStatus.TOPIC_NOT_ACTIVE);
        }

        TopicAnswer a = answerRepository.findByTopicIdAndUserId(topicId, userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.ANSWER_NOT_FOUND));
        a.updateContent(req.content());
        return TopicAnswerResponseDto.Info.of(a);
    }

    public List<TopicAnswerResponseDto.Info> getTopicAnswers(Long topicId) {
        return answerRepository.findAllByTopicId(topicId)
                .stream()
                .map(TopicAnswerResponseDto.Info::of)
                .toList();
    }

    public List<TopicAnswerResponseDto.Info> getFamilyAnswers(Long familyId) {
        Topic current = topicRepository.findFirstByStatusAndActiveFromLessThanEqualAndActiveUntilGreaterThanOrderByActiveFromDesc(
                TopicStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now()
        ).orElseThrow(() -> new GeneralException(ErrorStatus.TOPIC_NOT_FOUND));

        return answerRepository.findFamilyAnswers(current.getId(), familyId)
                .stream()
                .map(TopicAnswerResponseDto.Info::of)
                .toList();
    }

    public List<TopicAnswerResponseDto.Info> getFamilyAnswersByTopic(Long topicId, Long familyId) {
        return answerRepository.findFamilyAnswersByTopic(topicId, familyId)
                .stream()
                .map(TopicAnswerResponseDto.Info::of)
                .toList();
    }

    public TopicResponseDto.FamilyAnswered getFamilyAnsweredTopics(Long familyId) {
        List<TopicAnswer> raws = answerRepository.findAllByFamilyId(familyId);

        Map<Topic, List<TopicAnswer>> grouped = raws.stream()
                .collect(Collectors.groupingBy(
                        TopicAnswer::getTopic,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<TopicResponseDto.Simple> topics = grouped.keySet()
                .stream()
                .map(TopicResponseDto.Simple::of)
                .toList();

        return TopicResponseDto.FamilyAnswered.of(topics);
    }

    @Transactional
    public TopicResponseDto.Simple createDailyTopicFromGemini(TopicType type, List<String> recentQuestions) {

        String question = geminiService.generateDailyQuestion(type, recentQuestions);

        LocalDateTime from = LocalDateTime.now();
        LocalDateTime until = from.plusDays(3);

        Topic topic = Topic.builder()
                .question(question)
                .activeFrom(from)
                .activeUntil(until)
                .type(type)
                .build();

        return TopicResponseDto.Simple.of(topicRepository.save(topic));
    }
}