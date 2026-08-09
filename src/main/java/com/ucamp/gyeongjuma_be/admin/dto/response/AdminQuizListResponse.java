package com.ucamp.gyeongjuma_be.admin.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record AdminQuizListResponse(
        long totalCnt,
        int page,
        int size,
        int totalPages,
        List<AdminQuizSetDto> quizSets
) {
}
