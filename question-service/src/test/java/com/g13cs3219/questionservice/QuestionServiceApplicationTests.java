package com.g13cs3219.questionservice;

import com.g13cs3219.questionservice.model.Question;
import com.g13cs3219.questionservice.repositories.QuestionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class QuestionServiceApplicationTests {

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void canSaveAndFindQuestion() {
        Question q = Question.builder()
                .title("Two Sum")
                .topic("Arrays")
                .difficulty("easy")
                .prompt("Given an array, return indices of two numbers that add up to target.")
                .isActive(true)
                .build();

        Question saved = questionRepository.save(q);
        assertThat(saved.getQuestionId()).isNotNull();
        assertThat(questionRepository.findByQuestionIdAndIsActiveTrue(saved.getQuestionId())).isPresent();
    }

    @Test
    void duplicateTitleIsDetected() {
        Question q1 = Question.builder()
                .title("Unique Title Test")
                .topic("Arrays")
                .difficulty("easy")
                .prompt("Some prompt")
                .isActive(true)
                .build();
        questionRepository.save(q1);

        assertThat(questionRepository.existsByTitleIgnoreCaseAndIsActiveTrue("Unique Title Test")).isTrue();
        assertThat(questionRepository.existsByTitleIgnoreCaseAndIsActiveTrue("unique title test")).isTrue();
    }

    @Test
    void softDeletedQuestionNotFound() {
        Question q = Question.builder()
                .title("To Be Deleted")
                .topic("Graphs")
                .difficulty("hard")
                .prompt("Some prompt")
                .isActive(false)
                .build();
        Question saved = questionRepository.save(q);

        assertThat(questionRepository.findByQuestionIdAndIsActiveTrue(saved.getQuestionId())).isEmpty();
    }

    @Test
    void filterByDifficulty() {
        Question easy = Question.builder()
                .title("Easy Question CI")
                .topic("Sorting")
                .difficulty("easy")
                .prompt("prompt")
                .isActive(true)
                .build();
        questionRepository.save(easy);

        assertThat(questionRepository.findByDifficultyIgnoreCaseAndIsActiveTrue("easy")).isNotEmpty();
        assertThat(questionRepository.findByDifficultyIgnoreCaseAndIsActiveTrue("EASY")).isNotEmpty();
    }
}