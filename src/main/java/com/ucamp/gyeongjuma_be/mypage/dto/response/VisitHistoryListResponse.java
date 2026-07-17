package com.ucamp.gyeongjuma_be.mypage.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record VisitHistoryListResponse(
        int totalCnt,
        List<VisitHistoryDto> visits
) {
}
