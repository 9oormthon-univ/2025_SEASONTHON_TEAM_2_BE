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

    Optional<TopicAnswer> findByTopicIdAndUserId(Long topicId, Long userId);

    @Query("select ta from TopicAnswer ta where ta.topic.id = :topicId order by GREATEST(ta.updatedAt, ta.createdAt) desc")
    List<TopicAnswer> findAllByTopicIdOrderByMostRecent(@Param("topicId") Long topicId);

    @Query("""
      select ta from TopicAnswer ta
      join fetch ta.user u
      where ta.topic.id = :topicId
        and ta.familyId = :familyId
      order by GREATEST(ta.updatedAt, ta.createdAt) desc
    """)
    List<TopicAnswer> findFamilyAnswersByTopic(@Param("topicId") Long topicId, @Param("familyId") Long familyId);

    @Query("""
      select ta from TopicAnswer ta
      join fetch ta.user u
      where ta.topic.id = :activeTopicId
        and ta.familyId = :familyId
      order by GREATEST(ta.updatedAt, ta.createdAt) desc
    """)
    List<TopicAnswer> findFamilyAnswers(@Param("activeTopicId") Long activeTopicId, @Param("familyId") Long familyId);

    @Query("""
      select ta from TopicAnswer ta
      join fetch ta.user u
      join fetch ta.topic t
      where ta.familyId = :familyId
      order by t.activeFrom desc, GREATEST(ta.updatedAt, ta.createdAt) desc
    """)
    List<TopicAnswer> findAllByFamilyId(@Param("familyId") Long familyId);

    long countByUserId(Long userId);

    @Query("""
      select u.id, count(ta)
      from TopicAnswer ta join ta.user u
      where ta.familyId = :familyId
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
      where ta.familyId = :familyId and t.status = :status
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
      where ta.familyId = :familyId and ta.createdAt >= :from
      group by u.id
    """)
    List<Object[]> countSinceByFamilyGroup(@Param("familyId") Long familyId, @Param("from") LocalDateTime from);

    void deleteAllByUserId(Long userId);
}