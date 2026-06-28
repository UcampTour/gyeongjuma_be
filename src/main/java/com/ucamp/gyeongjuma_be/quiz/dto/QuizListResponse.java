package com.ucamp.gyeongjuma_be.quiz.dto;

import com.ucamp.gyeongjuma_be.quiz.domain.Quiz;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuizListResponse {
    private Long quizId;
    private String question;
    private LocalDateTime createdAt;

    public QuizListResponse(Quiz quiz) {
        this.quizId = quiz.getQuizId();
        this.question = quiz.getQuestion();
        this.createdAt = quiz.getCreatedAt();
    }
}
