package com.ucamp.gyeongjuma_be.member.dto.request;

public record LoginRequest(
        // GOOGLE(기본), KAKAO, NAVER
        String provider,

        // GOOGLE용 id_token
        String idToken,

        // KAKAO / NAVER용 access_token
        String accessToken
) {
    public String providerOrDefault() {
        return (provider == null || provider.isBlank()) ? "GOOGLE" : provider.toUpperCase();
    }

    /**
     * provider에 맞는 토큰을 선택한다. (GOOGLE → idToken, KAKAO/NAVER → accessToken)
     * 해당 필드가 비어 있으면 다른 쪽 필드를 fallback으로 사용한다.
     */
    public String resolveToken() {
        boolean isGoogle = "GOOGLE".equals(providerOrDefault());
        String primary = isGoogle ? idToken : accessToken;
        String fallback = isGoogle ? accessToken : idToken;
        return (primary != null && !primary.isBlank()) ? primary : fallback;
    }
}
