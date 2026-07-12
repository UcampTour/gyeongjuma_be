package com.ucamp.gyeongjuma_be.quiz.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class QuizResultResponse {

    private Long quizId;
    private Integer totalQuestions;
    private Integer correctQuestions;
    private Integer points;
    private List<QuizResult> questions;

    @Data
    public static class QuizResult {
        private Long questionId;
        private Boolean isCorrect;
        private Long userSelectedId;
        private Long correctOptionId;
    }
}
