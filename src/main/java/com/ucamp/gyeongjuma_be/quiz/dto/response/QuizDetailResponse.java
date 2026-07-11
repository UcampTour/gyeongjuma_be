package com.ucamp.gyeongjuma_be.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizDetailResponse {

    private Long quizId;
    private Long placeId;
    private String imageUrl;
    private String title;
    private String description;
    private Integer correctQuestions;
    private Integer lastQuestionIndex;
    private String quizStatus;
    private List<quizQuestion> questions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class quizQuestion {
        private Long quizId;
        private String question;
        private Boolean isSolved;
        private Boolean isCorrect;
        private List<quizOption> options;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class quizOption {
        private Long answerId;
        private String content;
    }
}
