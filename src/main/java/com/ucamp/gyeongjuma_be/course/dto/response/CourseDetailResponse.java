package com.ucamp.gyeongjuma_be.course.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record CourseDetailResponse(
        Long courseId,
        String title,
        String description,
        String type,
        String thumbnailUrl,
        Long placeCnt,
        List<CoursePlaceDto> courseList
) {
}
