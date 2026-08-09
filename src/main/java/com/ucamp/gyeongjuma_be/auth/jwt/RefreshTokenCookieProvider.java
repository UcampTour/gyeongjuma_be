package com.ucamp.gyeongjuma_be.auth.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 리프레시 토큰을 httpOnly 쿠키로 발급·삭제한다.
 * 자바스크립트에서 읽을 수 없으므로 XSS로 토큰을 탈취당하지 않는다.
 *
 * 배포 환경에 따라 secure/same-site 값을 properties에서 바꾼다.
 * - 프론트·백엔드가 같은 상위 도메인(www / api)  → same-site=Lax, secure=true
 * - 완전히 다른 도메인                          → same-site=None, secure=true (iOS 사파리에서 차단될 수 있음)
 */
@Component
public class RefreshTokenCookieProvider {

    public static final String COOKIE_NAME = "refreshToken";

    /** 회원 API 외의 경로(마이페이지 등)에는 쿠키를 보내지 않아 노출 범위를 좁힌다 */
    private static final String COOKIE_PATH = "/api/members";

    private final boolean secure;
    private final String sameSite;
    private final Duration maxAge;

    public RefreshTokenCookieProvider(
            @Value("${app.cookie.refresh.secure:false}") boolean secure,
            @Value("${app.cookie.refresh.same-site:Lax}") String sameSite,
            @Value("${jwt.refresh-token-validity-ms:1209600000}") long refreshTokenValidityMs) {
        // SameSite=None은 Secure가 없으면 브라우저가 쿠키를 거부하므로 강제로 켠다
        this.secure = secure || "None".equalsIgnoreCase(sameSite);
        this.sameSite = sameSite;
        this.maxAge = Duration.ofMillis(refreshTokenValidityMs);
    }

    public ResponseCookie create(String refreshToken) {
        return build(refreshToken, maxAge);
    }

    /** 로그아웃·탈퇴 시 즉시 만료시켜 브라우저에서 삭제한다 */
    public ResponseCookie clear() {
        return build("", Duration.ZERO);
    }

    private ResponseCookie build(String value, Duration age) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(COOKIE_PATH)
                .maxAge(age)
                .build();
    }
}
