package com.g13cs3219.server.repository;

import com.g13cs3219.server.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByIsActiveTrue();
    List<Question> findByTopicIgnoreCaseAndIsActiveTrue(String topic);
    List<Question> findByDifficultyIgnoreCaseAndIsActiveTrue(String difficulty);
    List<Question> findByTopicIgnoreCaseAndDifficultyIgnoreCaseAndIsActiveTrue(String topic, String difficulty);
    Optional<Question> findByQuestionIdAndIsActiveTrue(Long questionId);
    @Query(value = "SELECT * FROM questions WHERE LOWER(topic) = LOWER(:topic) " +
            "AND LOWER(difficulty) = LOWER(:difficulty) " +
            "AND is_active = true ORDER BY RANDOM() LIMIT 1",
            nativeQuery = true)
    Optional<Question> findRandomByTopicAndDifficulty(@Param("topic") String topic,
                                                      @Param("difficulty") String difficulty);

    boolean existsByTitleIgnoreCaseAndIsActiveTrue(String title);
}