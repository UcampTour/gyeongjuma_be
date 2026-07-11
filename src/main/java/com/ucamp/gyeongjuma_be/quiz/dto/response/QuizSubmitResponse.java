package com.ucamp.gyeongjuma_be.quiz.dto.response;

import lombok.Data;

@Data
public class QuizSubmitResponse {

    private Boolean isCorrect;
    private Boolean isLastQuestion;
    private Long correctAnswerId;
}
