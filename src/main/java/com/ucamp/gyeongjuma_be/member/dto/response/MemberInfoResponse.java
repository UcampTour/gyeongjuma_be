package com.ucamp.gyeongjuma_be.member.dto.response;

import lombok.Builder;

@Builder
public record MemberInfoResponse(
        Long memberId,
        String nickname,
        String profileImage,
        String difficulty,
        String locale
) {
}
