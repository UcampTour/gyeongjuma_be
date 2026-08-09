package com.ucamp.gyeongjuma_be.auth;

import com.ucamp.gyeongjuma_be.admin.repository.AdminMemberRepository;
import com.ucamp.gyeongjuma_be.common.exception.CustomException;
import com.ucamp.gyeongjuma_be.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * /api/admin/** 접근 시 관리자 권한을 확인한다.
 * AuthInterceptor가 먼저 실행되어 memberId를 심어준 뒤 이 인터셉터가 role을 검사한다.
 */
@Component
@RequiredArgsConstructor
public class AdminInterceptor implements HandlerInterceptor {

    private static final String ROLE_ADMIN = "ADMIN";

    private final AdminMemberRepository adminMemberRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        Object memberId = request.getAttribute(AuthInterceptor.MEMBER_ID_ATTRIBUTE);
        if (memberId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        String role = adminMemberRepository.findRoleById((Long) memberId);
        if (!ROLE_ADMIN.equals(role)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        return true;
    }
}
