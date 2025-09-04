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
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TopicService {

    private final TopicRepository topicRepository;
    private final TopicAnswerRepository answerRepository;
    private final UserRepository userRepository;

    // 토픽 등록 (3일짜리)
    @Transactional
    public TopicResponse createTopic(TopicCreateRequest req) {
        LocalDateTime from = req.activeFrom() != null ? req.activeFrom() : LocalDateTime.now();
        LocalDateTime until = from.plusDays(3);
        Topic topic = Topic.builder()
                .question(req.question())
                .activeFrom(from)
                .activeUntil(until)
                .build();
        topic.activate(); // 바로 활성화할지, 운영 정책에 맞춰 조정
        return TopicResponse.of(topicRepository.save(topic));
    }

    // 토픽 수정(문구 수정)
    @Transactional
    public TopicResponse updateTopic(Long topicId, TopicUpdateRequest req) {
        Topic t = topicRepository.findById(topicId).orElseThrow();
        t.updateQuestion(req.question());
        return TopicResponse.of(t);
    }

    // 현재 활성 토픽 조회
    public TopicResponse getCurrentActiveTopic() {
        Topic t = topicRepository.findFirstByStatusAndActiveFromLessThanEqualAndActiveUntilGreaterThanOrderByActiveFromDesc(
                TopicStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now()).orElseThrow();
        return TopicResponse.of(t);
    }

    // 답변 생성/수정
    @Transactional
    public AnswerResponse createAnswer(Long topicId, Long userId, AnswerCreateRequest req) {
        Topic topic = topicRepository.findById(topicId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        // 이미 있으면 업데이트로 동작시켜도 됨
        return answerRepository.findByTopicIdAndUserId(topicId, userId)
                .map(ans -> { ans.updateContent(req.content()); return AnswerResponse.of(ans); })
                .orElseGet(() -> {
                    TopicAnswer a = TopicAnswer.builder()
                            .topic(topic).user(user).content(req.content()).build();
                    return AnswerResponse.of(answerRepository.save(a));
                });
    }

    @Transactional
    public AnswerResponse updateAnswer(Long topicId, Long userId, AnswerUpdateRequest req) {
        TopicAnswer a = answerRepository.findByTopicIdAndUserId(topicId, userId).orElseThrow();
        a.updateContent(req.content());
        return AnswerResponse.of(a);
    }

    // 특정 토픽 답변 상세(본인 포함)
    public List<AnswerResponse> getTopicAnswers(Long topicId) {
        return answerRepository.findAllByTopicId(topicId).stream().map(AnswerResponse::of).toList();
    }

    // 가족이 작성한 활성 토픽(오늘의 질문) 답변 목록
    public List<AnswerResponse> getFamilyAnswers(Long familyId) {
        Topic current = topicRepository.findFirstByStatusAndActiveFromLessThanEqualAndActiveUntilGreaterThanOrderByActiveFromDesc(
                TopicStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now()).orElseThrow();
        return answerRepository.findFamilyAnswers(current.getId(), familyId).stream().map(AnswerResponse::of).toList();
    }

    // 가족이 과거에 남긴 "모든" 토픽과 그 답변을 묶어서 반환
    public List<TopicWithAnswersResponse> getFamilyAnswersByTopic(Long familyId) {
        // 가족 구성원들이 남긴 모든 답변(토픽 섞임) 조회
        List<TopicAnswer> raws = answerRepository.findAllByFamilyId(familyId);

        // 토픽별로 그룹핑
        java.util.Map<Topic, List<TopicAnswer>> grouped =
                raws.stream().collect(java.util.stream.Collectors.groupingBy(
                        TopicAnswer::getTopic,
                        java.util.LinkedHashMap::new,
                        java.util.stream.Collectors.toList()
                ));

        // (토픽, 해당 토픽의 가족 답변들) → DTO로 변환
        return grouped.entrySet().stream()
                .map(e -> {
                    Topic topic = e.getKey();
                    List<AnswerResponse> answers = e.getValue().stream()
                            .map(AnswerResponse::of)
                            .toList();
                    return TopicWithAnswersResponse.of(topic, answers);
                })
                .toList();
    }
}