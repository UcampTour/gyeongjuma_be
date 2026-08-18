package com.ucamp.gyeongjuma_be.admin.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record AdminCourseListResponse(
        long totalCnt,
        int page,
        int size,
        int totalPages,
        List<AdminCourseDto> courses
) {
}
