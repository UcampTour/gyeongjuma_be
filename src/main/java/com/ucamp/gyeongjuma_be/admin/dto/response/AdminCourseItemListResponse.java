package com.ucamp.gyeongjuma_be.admin.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record AdminCourseItemListResponse(
        long totalCnt,
        int page,
        int size,
        int totalPages,
        List<AdminCourseItemDto> courses
) {
}
