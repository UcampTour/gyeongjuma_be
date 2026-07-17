package com.ucamp.gyeongjuma_be.mypage.dto.response;

import lombok.Builder;

@Builder
public record MyPageInfoResponse(
        Long memberId,
        String nickname,
        String profileImage,
        String difficulty,
        Long point,
        Long distance,
        Long visitPlaceCnt
) {
}
