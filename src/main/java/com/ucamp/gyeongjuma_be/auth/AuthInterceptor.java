package com.ucamp.gyeongjuma_be.auth;

import com.ucamp.gyeongjuma_be.auth.jwt.JwtTokenProvider;
import com.ucamp.gyeongjuma_be.common.exception.CustomException;
import com.ucamp.gyeongjuma_be.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Authorization: Bearer {accessToken} 헤더를 검증하고
 * request attribute("memberId")에 회원 ID를 저장한다.
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    public static final String MEMBER_ID_ATTRIBUTE = "memberId";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        String token = header.substring(BEARER_PREFIX.length());
        if (!jwtTokenProvider.isAccessToken(token)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        request.setAttribute(MEMBER_ID_ATTRIBUTE, jwtTokenProvider.getMemberId(token));
        return true;
    }
}
