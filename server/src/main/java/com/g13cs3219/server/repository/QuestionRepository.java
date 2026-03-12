package com.g13cs3219.server.repository;

import com.g13cs3219.server.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByTopicAndIsActiveTrue(String topic);
    List<Question> findByTopicAndIsActiveTrueAndTitleContainingIgnoreCase(String topic, String title);
}