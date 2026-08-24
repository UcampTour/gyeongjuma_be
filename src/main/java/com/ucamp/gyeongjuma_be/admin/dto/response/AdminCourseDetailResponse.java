package com.ucamp.gyeongjuma_be.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record AdminCourseDetailResponse(
        Long courseId,
        String name,
        String description,
        String thumbnailUrl,
        Long placeCnt,
        Boolean isActive,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        List<AdminCoursePlaceDto> places
) {
}
