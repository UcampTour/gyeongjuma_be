package com.ucamp.gyeongjuma_be.admin.controller;

import com.ucamp.gyeongjuma_be.admin.dto.request.AdminLoginRequest;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminLoginResponse;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminLoginResult;
import com.ucamp.gyeongjuma_be.admin.service.AdminAuthService;
import com.ucamp.gyeongjuma_be.auth.jwt.RefreshTokenCookieProvider;
import com.ucamp.gyeongjuma_be.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final RefreshTokenCookieProvider refreshTokenCookieProvider;

    /**
     * 관리자 마스터 계정 로그인.
     * 일반 회원 로그인과 동일하게 리프레시 토큰은 httpOnly 쿠키로 내려간다.
     * 로그아웃·토큰 재발급은 회원 API(/api/members/logout, /api/members/reissue)를 그대로 쓴다.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AdminLoginResponse>> login(
            @Valid @RequestBody AdminLoginRequest request) {
        AdminLoginResult result = adminAuthService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookieProvider.create(result.refreshToken()).toString())
                .body(ApiResponse.success("관리자 로그인에 성공했습니다.", result.toResponse()));
    }
}
