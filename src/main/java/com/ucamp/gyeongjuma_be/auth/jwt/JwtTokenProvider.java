package com.ucamp.gyeongjuma_be.auth.jwt;

import com.ucamp.gyeongjuma_be.common.exception.CustomException;
import com.ucamp.gyeongjuma_be.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtTokenProvider {

    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";
    private static final String CLAIM_TYPE = "type";

    private final SecretKey key;
    private final long accessTokenValidityMs;
    private final long refreshTokenValidityMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-ms:3600000}") long accessTokenValidityMs,
            @Value("${jwt.refresh-token-validity-ms:1209600000}") long refreshTokenValidityMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityMs = accessTokenValidityMs;
        this.refreshTokenValidityMs = refreshTokenValidityMs;
    }

    public String createAccessToken(Long memberId) {
        return createToken(memberId, TOKEN_TYPE_ACCESS, accessTokenValidityMs);
    }

    public String createRefreshToken(Long memberId) {
        return createToken(memberId, TOKEN_TYPE_REFRESH, refreshTokenValidityMs);
    }

    /**
     * 지금 발급하는 리프레시 토큰의 만료 시각 (DB 저장용)
     */
    public LocalDateTime getRefreshTokenExpiredAt() {
        return LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusSeconds(refreshTokenValidityMs / 1000);
    }

    private String createToken(Long memberId, String tokenType, long validityMs) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim(CLAIM_TYPE, tokenType)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + validityMs))
                .signWith(key)
                .compact();
    }

    /**
     * 토큰 검증 후 memberId 반환. 유효하지 않으면 CustomException 발생.
     */
    public Long getMemberId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    public boolean isAccessToken(String token) {
        return TOKEN_TYPE_ACCESS.equals(parseClaims(token).get(CLAIM_TYPE, String.class));
    }

    public boolean isRefreshToken(String token) {
        return TOKEN_TYPE_REFRESH.equals(parseClaims(token).get(CLAIM_TYPE, String.class));
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }
}
