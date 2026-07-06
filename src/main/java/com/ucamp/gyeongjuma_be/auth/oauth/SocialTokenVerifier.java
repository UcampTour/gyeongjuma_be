package com.ucamp.gyeongjuma_be.auth.oauth;

/**
 * 소셜 로그인 토큰 검증기 공통 인터페이스.
 * GOOGLE은 id_token, KAKAO/NAVER는 access_token을 검증한다.
 */
public interface SocialTokenVerifier {

    /** 지원하는 provider 이름 (GOOGLE, KAKAO, NAVER) */
    String provider();

    /** 토큰을 검증하고 사용자 정보를 반환한다. 실패 시 CustomException(SOCIAL_LOGIN_FAILED) */
    SocialUserInfo verify(String token);
}
