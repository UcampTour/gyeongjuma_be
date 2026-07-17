package com.ucamp.gyeongjuma_be.member.dto.response;

import lombok.Builder;

@Builder
public record LoginResponse(
        Long memberId,
        String email,
        String nickname,
        boolean isNewMember,
        String accessToken,
        String refreshToken
) {
}
