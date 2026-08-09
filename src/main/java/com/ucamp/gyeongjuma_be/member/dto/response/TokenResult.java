package com.ucamp.gyeongjuma_be.member.dto.response;

import lombok.Builder;

/**
 * 서비스 → 컨트롤러 내부 전달용. 리프레시 토큰은 쿠키로 나간다.
 */
@Builder
public record TokenResult(
        String accessToken,
        String refreshToken
) {
}
