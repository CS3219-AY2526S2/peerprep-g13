package com.g13cs3219.questionservice.services;

import com.g13cs3219.questionservice.dto.requests.QuestionRequest;
import com.g13cs3219.questionservice.dto.responses.QuestionResponse;
import com.g13cs3219.questionservice.model.Question;
import com.g13cs3219.questionservice.repositories.QuestionRepository;
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
        boolean hasTopic      = topic      != null && !topic.isBlank();
        boolean hasDifficulty = difficulty != null && !difficulty.isBlank();

        List<Question> questions;
        if (hasTopic && hasDifficulty) {
            questions = questionRepository.findByTopicIgnoreCaseAndDifficultyIgnoreCaseAndIsActiveTrue(topic, difficulty);
        } else if (hasTopic) {
            questions = questionRepository.findByTopicIgnoreCaseAndIsActiveTrue(topic);
        } else if (hasDifficulty) {
            questions = questionRepository.findByDifficultyIgnoreCaseAndIsActiveTrue(difficulty);
        } else {
            questions = questionRepository.findByIsActiveTrue();
        }

        return questions.stream().map(QuestionResponse::from).collect(Collectors.toList());
    }

    public QuestionResponse getQuestionById(Long id) {
        Question question = questionRepository.findByQuestionIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found with id: " + id));
        return QuestionResponse.from(question);
    }

    public Long createQuestion(QuestionRequest req, Long createdBy) {
        validateRequired(req);
        validateDifficulty(req.getDifficulty());

        if (questionRepository.existsByTitleIgnoreCaseAndIsActiveTrue(req.getTitle())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A question with this title already exists.");
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

        return questionRepository.save(question).getQuestionId();
    }

    public void updateQuestion(Long id, QuestionRequest req, Long updatedBy) {
        Question question = questionRepository.findByQuestionIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found with id: " + id));

        if (req.getTitle() != null && !req.getTitle().isBlank()) {
            if (!req.getTitle().equalsIgnoreCase(question.getTitle())
                    && questionRepository.existsByTitleIgnoreCaseAndIsActiveTrue(req.getTitle())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A question with this title already exists.");
            }
            question.setTitle(req.getTitle());
        }
        if (req.getTopic()      != null && !req.getTopic().isBlank())  question.setTopic(req.getTopic());
        if (req.getDifficulty() != null) { validateDifficulty(req.getDifficulty()); question.setDifficulty(req.getDifficulty().toLowerCase()); }
        if (req.getPrompt()      != null && !req.getPrompt().isBlank())  question.setPrompt(req.getPrompt());
        if (req.getExample()     != null) question.setExample(req.getExample());
        if (req.getConstraints() != null) question.setConstraints(req.getConstraints());
        if (req.getImageUrls()   != null) question.setImageUrls(req.getImageUrls());

        question.setUpdatedBy(updatedBy);
        questionRepository.save(question);
    }

    public void deleteQuestion(Long id) {
        Question question = questionRepository.findByQuestionIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found with id: " + id));
        question.setIsActive(false);
        questionRepository.save(question);
    }

    public QuestionResponse matchQuestion(String topic, String difficulty) {
        String normalizedTopic = (topic == null) ? null : topic.trim();
        if (normalizedTopic == null || normalizedTopic.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "topic is required.");
        }
        validateDifficulty(difficulty);
        return questionRepository.findRandomByTopicAndDifficulty(normalizedTopic, difficulty)
                .map(QuestionResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No question found for topic='" + normalizedTopic + "' difficulty='" + difficulty + "'"));
    }

    private void validateRequired(QuestionRequest req) {
        if (req.getTitle() == null || req.getTitle().isBlank() ||
                req.getTopic() == null || req.getTopic().isBlank() ||
                req.getDifficulty() == null || req.getDifficulty().isBlank() ||
                req.getPrompt() == null || req.getPrompt().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title, topic, difficulty and prompt are required.");
        }
    }

    private void validateDifficulty(String difficulty) {
        if (difficulty == null || difficulty.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "difficulty is required and must be one of: easy, medium, hard");
        }
        if (!List.of("easy", "medium", "hard").contains(difficulty.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "difficulty must be one of: easy, medium, hard");
        }
    }
}
