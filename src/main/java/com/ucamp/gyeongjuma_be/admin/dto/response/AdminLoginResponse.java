package com.ucamp.gyeongjuma_be.admin.dto.response;

import lombok.Builder;

@Builder
public record AdminLoginResponse(
        Long memberId,
        String username,
        String nickname,
        String role,
        String accessToken
) {
}
