package com.seasonthon.everflow.app.topic.repository;

import com.seasonthon.everflow.app.topic.domain.Topic;
import com.seasonthon.everflow.app.topic.domain.TopicStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    Optional<Topic> findFirstByStatusAndActiveFromLessThanEqualAndActiveUntilGreaterThanOrderByActiveFromDesc(
            TopicStatus status, LocalDateTime now1, LocalDateTime now2
    );

    // TopicRepository.java
    List<Topic> findByStatusAndActiveUntilBefore(TopicStatus status, LocalDateTime time);
}