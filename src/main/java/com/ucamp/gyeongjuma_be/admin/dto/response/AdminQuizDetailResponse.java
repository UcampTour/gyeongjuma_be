package com.ucamp.gyeongjuma_be.admin.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record AdminQuizDetailResponse(
        AdminQuizSetDto quizSet,
        int questionCnt,
        List<AdminQuizQuestionDto> questions
) {
}
