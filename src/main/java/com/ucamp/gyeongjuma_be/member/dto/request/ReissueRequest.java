package com.ucamp.gyeongjuma_be.member.dto.request;

/**
 * 리프레시 토큰은 httpOnly 쿠키로 전달받는다.
 * 이 바디는 프론트가 쿠키 방식으로 전환하기 전까지만 쓰는 과도기 fallback이며,
 * 전환이 끝나면 이 record와 컨트롤러의 fallback 처리를 함께 제거한다.
 */
public record ReissueRequest(
        String refreshToken
) {
}
