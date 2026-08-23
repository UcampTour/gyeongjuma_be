package com.ucamp.gyeongjuma_be.admin.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class AdminQuizQuestionDto {

    private Long quizId;
    private String question;
    /** 이 문항이 번역본이면 원본 문항 ID, 원본이면 null */
    private Long originQuizId;
    private Boolean isActive;
    private List<AdminQuizAnswerDto> answers;
}
