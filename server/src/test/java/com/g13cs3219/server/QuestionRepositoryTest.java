package com.g13cs3219.server;

import com.g13cs3219.server.model.Question;
import com.g13cs3219.server.repository.QuestionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class QuestionRepositoryTest {

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    void testSaveQuestion() {
        Question question = new Question();
        question.setTitle("Test Question");
        question.setTopic("Algorithms");
        question.setPrompt("Write a function that sums two numbers");
        question.setExample(List.of("Example 1"));
        question.setConstraints(List.of("No constraints"));
        question.setImageUrls(List.of());
        question.setCreatedAt(LocalDateTime.now());
        question.setCreatedBy(1L);
        question.setIsActive(true);

        Question saved = questionRepository.save(question);

        assertNotNull(saved.getQuestionId(), "QuestionId mock test successfully");
    }
}