package com.ucamp.gyeongjuma_be.course.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record CourseListResponse(
        int totalCnt,
        List<CourseDto> courses
) {
}
