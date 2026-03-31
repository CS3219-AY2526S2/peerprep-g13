package com.g13cs3219.questionservice.controllers;

import com.g13cs3219.questionservice.dto.requests.MatchRequest;
import com.g13cs3219.questionservice.dto.requests.QuestionRequest;
import com.g13cs3219.questionservice.dto.responses.QuestionResponse;
import com.g13cs3219.questionservice.services.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getQuestions(
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String difficulty) {
        List<QuestionResponse> questions = questionService.getQuestions(topic, difficulty);
        return ResponseEntity.ok(Map.of("questions", questions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getQuestionById(@PathVariable Long id) {
        QuestionResponse question = questionService.getQuestionById(id);
        return ResponseEntity.ok(Map.of("question", question));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUESTION_MASTER')")
    public ResponseEntity<Map<String, Object>> createQuestion(@RequestBody QuestionRequest req) {
        Long questionId = questionService.createQuestion(req, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("questionId", questionId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUESTION_MASTER')")
    public ResponseEntity<String> updateQuestion(@PathVariable Long id, @RequestBody QuestionRequest req) {
        questionService.updateQuestion(id, req, null);
        return ResponseEntity.ok("Question updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.ok("Question deleted successfully");
    }

    @PostMapping("/match")
    public ResponseEntity<Map<String, Object>> matchQuestion(@RequestBody MatchRequest req) {
        QuestionResponse question = questionService.matchQuestion(req.getTopic(), req.getDifficulty());
        return ResponseEntity.ok(Map.of("question", question));
    }
}
