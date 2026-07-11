package com.ucamp.gyeongjuma_be.quiz.dto.request;

import lombok.Data;

@Data
public class QuizSubmitRequest {

    private Long questionId;
    private Long selectedOptionId;
}
