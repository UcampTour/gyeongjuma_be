package com.ucamp.gyeongjuma_be.admin.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class AdminQuizQuestionDto {

    private Long quizId;
    private String question;
    private Boolean isActive;
    private List<AdminQuizAnswerDto> answers;
}
