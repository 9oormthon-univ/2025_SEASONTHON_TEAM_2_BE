package com.seasonthon.everflow.app.topic.repository;

import com.seasonthon.everflow.app.topic.domain.TopicAnswer;
import com.seasonthon.everflow.app.topic.domain.TopicStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TopicAnswerRepository extends JpaRepository<TopicAnswer, Long> {

    // 단건
    Optional<TopicAnswer> findByTopicIdAndUserId(Long topicId, Long userId);

    // 특정 토픽 전체 답변
    List<TopicAnswer> findAllByTopicId(Long topicId);

    // 가족이 작성한 활성 토픽 답변
    @Query("""
      select ta from TopicAnswer ta
      where ta.topic.id = :topicId
        and ta.user.family.id = :familyId
    """)
    List<TopicAnswer> findFamilyAnswersByTopic(@Param("topicId") Long topicId, @Param("familyId") Long familyId);

    @Query("""
      select ta from TopicAnswer ta
      where ta.topic.id = :topicId
        and ta.user.family.id = :familyId
    """)
    List<TopicAnswer> findFamilyAnswers(@Param("topicId") Long activeTopicId, @Param("familyId") Long familyId);

    // 가족이 과거에 남긴 모든 답변(토픽 섞임)
    @Query("""
      select ta from TopicAnswer ta
      where ta.user.family.id = :familyId
      order by ta.createdAt asc
    """)
    List<TopicAnswer> findAllByFamilyId(@Param("familyId") Long familyId);

    // --- 홈 참여율 계산에 쓰는 집계용 (있으면 사용 / 없으면 생략 가능) ---

    long countByUserId(Long userId);

    @Query("""
      select u.id, count(ta)
      from TopicAnswer ta join ta.user u
      where u.family.id = :familyId
      group by u.id
    """)
    List<Object[]> countByFamilyGroup(@Param("familyId") Long familyId);

    @Query("""
      select count(ta)
      from TopicAnswer ta join ta.topic t
      where ta.user.id = :userId and t.status = :status
    """)
    long countActiveByUser(@Param("userId") Long userId, @Param("status") TopicStatus status);

    @Query("""
      select u.id, count(ta)
      from TopicAnswer ta join ta.user u join ta.topic t
      where u.family.id = :familyId and t.status = :status
      group by u.id
    """)
    List<Object[]> countActiveByFamilyGroup(@Param("familyId") Long familyId, @Param("status") TopicStatus status);

    @Query("""
      select count(ta) from TopicAnswer ta
      where ta.user.id = :userId and ta.createdAt >= :from
    """)
    long countSinceByUser(@Param("userId") Long userId, @Param("from") LocalDateTime from);

    @Query("""
      select u.id, count(ta)
      from TopicAnswer ta join ta.user u
      where u.family.id = :familyId and ta.createdAt >= :from
      group by u.id
    """)
    List<Object[]> countSinceByFamilyGroup(@Param("familyId") Long familyId, @Param("from") LocalDateTime from);
}