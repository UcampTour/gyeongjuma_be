package com.ucamp.gyeongjuma_be.admin.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record AdminMemberListResponse(
        long totalCnt,
        int page,
        int size,
        int totalPages,
        List<AdminMemberDto> members
) {
}
