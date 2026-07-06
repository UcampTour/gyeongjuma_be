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

    private Long placeId;
    private String placeName;
    private String imageUrl;
    private Integer totalQuestions;
    private Integer correctQuestions;
    private Integer progressRate;
    private List<QuestionResponse> questions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionResponse {
        private Long quizId;
        private String question;
        private Boolean isCorrect;
        private List<OptionResponse> options;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionResponse {
        private Long answerId;
        private String content;
    }
}
