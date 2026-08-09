package com.ucamp.gyeongjuma_be.member.dto.response;

import lombok.Builder;

/**
 * 리프레시 토큰은 httpOnly 쿠키로 내려가므로 응답 바디에 포함하지 않는다.
 */
@Builder
public record TokenResponse(
        String accessToken
) {
}
