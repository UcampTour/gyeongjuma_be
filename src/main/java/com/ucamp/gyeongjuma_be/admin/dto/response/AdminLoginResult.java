package com.ucamp.gyeongjuma_be.admin.dto.response;

import lombok.Builder;

/**
 * 서비스 → 컨트롤러 내부 전달용. 리프레시 토큰은 httpOnly 쿠키로 나간다.
 */
@Builder
public record AdminLoginResult(
        Long memberId,
        String username,
        String nickname,
        String role,
        String accessToken,
        String refreshToken
) {
    public AdminLoginResponse toResponse() {
        return AdminLoginResponse.builder()
                .memberId(memberId)
                .username(username)
                .nickname(nickname)
                .role(role)
                .accessToken(accessToken)
                .build();
    }
}
