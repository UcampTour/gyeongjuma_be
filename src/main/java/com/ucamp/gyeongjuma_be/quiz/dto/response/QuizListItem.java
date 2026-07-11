package com.ucamp.gyeongjuma_be.quiz.dto.response;

import lombok.Data;

@Data
public class QuizListItem {

    private Long quizId;
    private String quizTitle;
    private String description;
    private String imageUrl;
    private Integer totalQuestions;
    private Integer solvedQuestions;
    private String quizStatus;
}
