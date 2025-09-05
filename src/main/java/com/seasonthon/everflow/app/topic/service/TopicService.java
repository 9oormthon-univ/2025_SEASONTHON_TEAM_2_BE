package com.seasonthon.everflow.app.topic.service;

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

    // 1) 토픽 등록 (3일 활성)
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

    // 2) 토픽 수정(문구 수정)
    @Transactional
    public TopicResponse updateTopic(Long topicId, TopicUpdateRequest req) {
        Topic t = topicRepository.findById(topicId).orElseThrow();
        t.updateQuestion(req.question());
        return TopicResponse.of(t);
    }

    // 활성 토픽 조회
    public TopicResponse getCurrentActiveTopic() {
        Topic t = topicRepository.findFirstByStatusAndActiveFromLessThanEqualAndActiveUntilGreaterThanOrderByActiveFromDesc(
                TopicStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now()
        ).orElseThrow();
        return TopicResponse.of(t);
    }

    // 5) 답변 생성(내 것) - 존재 시 덮어쓰기
    @Transactional
    public AnswerResponse createAnswer(Long topicId, Long userId, AnswerCreateRequest req) {
        Topic topic = topicRepository.findById(topicId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        return answerRepository.findByTopicIdAndUserId(topicId, userId)
                .map(ans -> { ans.updateContent(req.content()); return AnswerResponse.of(ans); })
                .orElseGet(() -> {
                    TopicAnswer a = TopicAnswer.builder().topic(topic).user(user).content(req.content()).build();
                    return AnswerResponse.of(answerRepository.save(a));
                });
    }

    // 6) 답변 수정(내 것)
    @Transactional
    public AnswerResponse updateAnswer(Long topicId, Long userId, AnswerUpdateRequest req) {
        TopicAnswer a = answerRepository.findByTopicIdAndUserId(topicId, userId).orElseThrow();
        a.updateContent(req.content());
        return AnswerResponse.of(a);
    }

    // 특정 토픽의 모든 답변(공개)
    public List<AnswerResponse> getTopicAnswers(Long topicId) {
        return answerRepository.findAllByTopicId(topicId).stream().map(AnswerResponse::of).toList();
    }

    // 활성 토픽의 우리 가족 답변
    public List<AnswerResponse> getFamilyAnswers(Long familyId) {
        Topic current = topicRepository.findFirstByStatusAndActiveFromLessThanEqualAndActiveUntilGreaterThanOrderByActiveFromDesc(
                TopicStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now()
        ).orElseThrow();
        return answerRepository.findFamilyAnswers(current.getId(), familyId).stream().map(AnswerResponse::of).toList();
    }

    // 4) 특정 토픽의 우리 가족 답변
    public List<AnswerResponse> getFamilyAnswersByTopic(Long topicId, Long familyId) {
        return answerRepository.findFamilyAnswersByTopic(topicId, familyId).stream().map(AnswerResponse::of).toList();
    }

    // 3) 우리 가족이 답변 남긴 토픽 목록 & 개수
    public FamilyAnsweredTopicsResponse getFamilyAnsweredTopics(Long familyId) {
        // 가족 구성원들이 남긴 모든 답변 → 토픽 기준 중복 제거(첫 등장 순서 유지)
        List<TopicAnswer> raws = answerRepository.findAllByFamilyId(familyId);
        Map<Topic, List<TopicAnswer>> grouped = raws.stream()
                .collect(Collectors.groupingBy(TopicAnswer::getTopic, LinkedHashMap::new, Collectors.toList()));

        List<TopicResponse> topics = grouped.keySet().stream().map(TopicResponse::of).toList();
        return new FamilyAnsweredTopicsResponse(topics, topics.size());
    }
}