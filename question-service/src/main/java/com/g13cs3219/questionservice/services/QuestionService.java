package com.g13cs3219.questionservice.services;

import com.g13cs3219.questionservice.dto.requests.QuestionRequest;
import com.g13cs3219.questionservice.dto.responses.QuestionResponse;
import com.g13cs3219.questionservice.dto.responses.TopicResponse;
import com.g13cs3219.questionservice.model.Question;
import com.g13cs3219.questionservice.repositories.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service layer for the Question Service.
 *
 * Handles all business logic for managing the question repository,
 * including CRUD operations and question matching for the Matching Service.
 * All queries are scoped to active questions only (isActive = true).
 */
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    private static final Map<String, Integer> DIFFICULTY_ORDER = Map.of(
            "easy",   0,
            "medium", 1,
            "hard",   2
    );

    /**
     * Retrieves all active questions, with optional filtering by topic and/or difficulty.
     *
     * - If both topic and difficulty are provided, filters by both.
     * - If only one is provided, filters by that field only.
     * - If neither is provided, returns all active questions.
     *
     * @param topic      optional topic filter (case-insensitive), e.g. "Arrays"
     * @param difficulty optional difficulty filter (case-insensitive), e.g. "easy"
     * @return list of matching active questions as response DTOs, sorted by difficulty (easy → medium → hard)
     */
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

        return questions.stream()
                .sorted(Comparator.comparingInt(q ->
                        DIFFICULTY_ORDER.getOrDefault(q.getDifficulty().toLowerCase(), 99)))
                .map(QuestionResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single active question by its ID.
     *
     * @param id the question's primary key
     * @return the question as a response DTO
     * @throws ResponseStatusException 404 if no active question exists with the given ID
     */
    public QuestionResponse getQuestionById(Long id) {
        Question question = questionRepository.findByQuestionIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found with id: " + id));
        return QuestionResponse.from(question);
    }

    /**
     * Creates a new question and persists it to the database.
     *
     * Validates that all required fields (title, topic, difficulty, prompt) are present
     * and that difficulty is one of: easy, medium, hard.
     * Also checks that no active question with the same title already exists.
     *
     * @param req       the request body containing question details
     * @param createdBy the userId of the admin performing the action
     * @return the generated questionId of the newly created question
     * @throws ResponseStatusException 400 if required fields are missing or difficulty is invalid
     * @throws ResponseStatusException 409 if a question with the same title already exists
     */
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

    /**
     * Soft deletes a question by setting its isActive flag to false.
     *
     * The record is retained in the database to preserve referential integrity
     * with the History table.
     * Soft-deleted questions are excluded from all other queries.
     *
     * @param id the ID of the question to delete
     * @throws ResponseStatusException 404 if no active question exists with the given ID
     */
    public void deleteQuestion(Long id) {
        Question question = questionRepository.findByQuestionIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found with id: " + id));
        question.setIsActive(false);
        questionRepository.save(question);
    }

    /**
     * Used by Matching Service: returns one random question matching
     * the given topic + difficulty.
     */
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

    /**
     * returns a list of distinct topics
     */
    public TopicResponse getTopics() {
        List<String> topics = questionRepository.findAllDistinctTopics();
        if (topics.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No topics found.");
        }

        return TopicResponse.from(topics);
    }

    /**
     * Validates that all required fields for creating a question are present and non-blank.
     *
     * @param req the question creation request
     * @throws ResponseStatusException 400 if any required field is missing
     */
    private void validateRequired(QuestionRequest req) {
        if (req.getTitle() == null || req.getTitle().isBlank() ||
                req.getTopic() == null || req.getTopic().isBlank() ||
                req.getDifficulty() == null || req.getDifficulty().isBlank() ||
                req.getPrompt() == null || req.getPrompt().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title, topic, difficulty and prompt are required.");
        }
    }

    /**
     * Validates that the difficulty value is one of the accepted values.
     *
     * @param difficulty the difficulty string to validate
     * @throws ResponseStatusException 400 if the value is not easy, medium, or hard
     */
    private void validateDifficulty(String difficulty) {
        if (difficulty == null || difficulty.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "difficulty is required and must be one of: easy, medium, hard");
        }
        if (!List.of("easy", "medium", "hard").contains(difficulty.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "difficulty must be one of: easy, medium, hard");
        }
    }
}