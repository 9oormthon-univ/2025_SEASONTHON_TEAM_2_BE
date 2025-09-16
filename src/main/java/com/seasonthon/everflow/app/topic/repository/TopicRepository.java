package com.seasonthon.everflow.app.topic.repository;

import com.seasonthon.everflow.app.topic.domain.Topic;
import com.seasonthon.everflow.app.topic.domain.TopicStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    Optional<Topic> findFirstByStatusAndActiveFromLessThanEqualAndActiveUntilGreaterThanOrderByActiveFromDesc(
            TopicStatus status, LocalDateTime from, LocalDateTime until
    );

    Optional<Topic> findFirstByStatusOrderByIdAsc(TopicStatus status);

    List<Topic> findByStatusAndActiveUntilBefore(TopicStatus status, LocalDateTime time);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
   update Topic t
      set t.status = com.seasonthon.everflow.app.topic.domain.TopicStatus.EXPIRED
    where t.status = com.seasonthon.everflow.app.topic.domain.TopicStatus.ACTIVE
      and t.activeUntil <= :now
""")
    int bulkExpire(@Param("now") LocalDateTime now);

    List<Topic> findTop5ByOrderByIdDesc();
}