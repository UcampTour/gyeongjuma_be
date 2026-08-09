package com.ucamp.gyeongjuma_be.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class AdminQuizAnswerDto {

    private Long answerId;
    private String content;
    private Boolean isCorrect;

    /** 문항별로 묶기 위한 값이라 응답에는 내보내지 않는다 */
    @JsonIgnore
    private Long quizId;
}
