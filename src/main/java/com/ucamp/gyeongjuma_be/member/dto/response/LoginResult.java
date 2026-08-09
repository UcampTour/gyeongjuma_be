package com.ucamp.gyeongjuma_be.member.dto.response;

import lombok.Builder;

/**
 * 서비스 → 컨트롤러 내부 전달용. 리프레시 토큰은 응답 바디가 아니라 쿠키로 나가므로
 * API 응답 DTO(LoginResponse)와 분리한다.
 */
@Builder
public record LoginResult(
        Long memberId,
        String email,
        String nickname,
        boolean isNewMember,
        String accessToken,
        String refreshToken
) {
    public LoginResponse toResponse() {
        return LoginResponse.builder()
                .memberId(memberId)
                .email(email)
                .nickname(nickname)
                .isNewMember(isNewMember)
                .accessToken(accessToken)
                .build();
    }
}
