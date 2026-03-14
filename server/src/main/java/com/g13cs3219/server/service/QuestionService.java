package com.g13cs3219.server.service;

import com.g13cs3219.server.dto.QuestionRequest;
import com.g13cs3219.server.dto.QuestionResponse;
import com.g13cs3219.server.model.Question;
import com.g13cs3219.server.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    public List<QuestionResponse> getQuestions(String topic, String difficulty) {
        List<Question> questions;

        boolean hasTopic      = topic      != null && !topic.isBlank();
        boolean hasDifficulty = difficulty != null && !difficulty.isBlank();

        if (hasTopic && hasDifficulty) {
            questions = questionRepository.findByTopicIgnoreCaseAndDifficultyIgnoreCaseAndIsActiveTrue(topic, difficulty);
        } else if (hasTopic) {
            questions = questionRepository.findByTopicIgnoreCaseAndIsActiveTrue(topic);
        } else if (hasDifficulty) {
            questions = questionRepository.findByDifficultyIgnoreCaseAndIsActiveTrue(difficulty);
        } else {
            questions = questionRepository.findByIsActiveTrue();
        }

        return questions.stream()
                .map(QuestionResponse::from)
                .collect(Collectors.toList());
    }

    public QuestionResponse getQuestionById(Long id) {
        Question question = questionRepository.findByQuestionIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Question not found with id: " + id));
        return QuestionResponse.from(question);
    }

    public Long createQuestion(QuestionRequest req, Long createdBy) {
        validateRequired(req);
        validateDifficulty(req.getDifficulty());

        if (questionRepository.existsByTitleIgnoreCaseAndIsActiveTrue(req.getTitle())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A question with this title already exists.");
        }

        Question question = Question.builder()
                .title(req.getTitle())
                .topic(req.getTopic())
                .difficulty(req.getDifficulty().toLowerCase())
                .prompt(req.getPrompt())
                .example(req.getExample())
                .constraints(req.getConstraints())
                .imageUrls(req.getImageUrls())
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .isActive(true)
                .build();

        Question saved = questionRepository.save(question);
        return saved.getQuestionId();
    }
    public void updateQuestion(Long id, QuestionRequest req, Long updatedBy) {
        Question question = questionRepository.findByQuestionIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Question not found with id: " + id));

        if (req.getTitle() != null && !req.getTitle().isBlank()) {
            if (!req.getTitle().equalsIgnoreCase(question.getTitle())
                    && questionRepository.existsByTitleIgnoreCaseAndIsActiveTrue(req.getTitle())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "A question with this title already exists.");
            }
            question.setTitle(req.getTitle());
        }
        if (req.getTopic()      != null) question.setTopic(req.getTopic());
        if (req.getDifficulty() != null) {
            validateDifficulty(req.getDifficulty());
            question.setDifficulty(req.getDifficulty().toLowerCase());
        }
        if (req.getPrompt()      != null) question.setPrompt(req.getPrompt());
        if (req.getExample()     != null) question.setExample(req.getExample());
        if (req.getConstraints() != null) question.setConstraints(req.getConstraints());
        if (req.getImageUrls()   != null) question.setImageUrls(req.getImageUrls());

        question.setUpdatedBy(updatedBy);
        questionRepository.save(question);
    }

    public void deleteQuestion(Long id) {
        Question question = questionRepository.findByQuestionIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Question not found with id: " + id));

        question.setIsActive(false);
        questionRepository.save(question);
    }
    /**
     * Used by Matching Service: returns one random question matching
     * the given topic + difficulty.
     */
    public QuestionResponse matchQuestion(String topic, String difficulty) {
        validateDifficulty(difficulty);

        return questionRepository.findRandomByTopicAndDifficulty(topic, difficulty)
                .map(QuestionResponse::from)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No question found for topic='" + topic + "' difficulty='" + difficulty + "'"));
    }

    private void validateRequired(QuestionRequest req) {
        if (req.getTitle()      == null || req.getTitle().isBlank()  ||
                req.getTopic()      == null || req.getTopic().isBlank()  ||
                req.getDifficulty() == null || req.getDifficulty().isBlank() ||
                req.getPrompt()     == null || req.getPrompt().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "title, topic, difficulty and prompt are required.");
        }
    }

    private void validateDifficulty(String difficulty) {
        if (!List.of("easy", "medium", "hard").contains(difficulty.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "difficulty must be one of: easy, medium, hard");
        }
    }
}