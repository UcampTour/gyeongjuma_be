package com.ucamp.gyeongjuma_be.mypage.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record StampListResponse(
        int totalCnt,
        int acquiredCnt,
        List<StampDto> stamps
) {
}
