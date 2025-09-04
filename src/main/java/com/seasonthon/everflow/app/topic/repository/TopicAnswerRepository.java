package com.seasonthon.everflow.app.topic.repository;

import com.seasonthon.everflow.app.topic.domain.Topic;
import com.seasonthon.everflow.app.topic.domain.TopicAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TopicAnswerRepository extends JpaRepository<TopicAnswer, Long> {
    Optional<TopicAnswer> findByTopicIdAndUserId(Long topicId, Long userId);

    List<TopicAnswer> findAllByTopicId(Long topicId);

    @Query("""
        select ta from TopicAnswer ta
        join ta.user u
        where ta.topic.id = :topicId and u.family.id = :familyId
        order by ta.createdAt desc
    """)
    List<TopicAnswer> findFamilyAnswers(Long topicId, Long familyId);

    @org.springframework.data.jpa.repository.Query("""
    select ta
    from TopicAnswer ta
    join ta.user u
    where u.family.id = :familyId
    order by ta.topic.activeFrom desc, ta.createdAt desc
""")
    java.util.List<com.seasonthon.everflow.app.topic.domain.TopicAnswer> findAllByFamilyId(Long familyId);
}
