package com.ucamp.gyeongjuma_be.member.controller;

import com.ucamp.gyeongjuma_be.auth.AuthInterceptor;
import com.ucamp.gyeongjuma_be.auth.jwt.RefreshTokenCookieProvider;
import com.ucamp.gyeongjuma_be.common.dto.ApiResponse;
import com.ucamp.gyeongjuma_be.common.exception.CustomException;
import com.ucamp.gyeongjuma_be.common.exception.ErrorCode;
import com.ucamp.gyeongjuma_be.member.dto.request.ExtraInfoRequest;
import com.ucamp.gyeongjuma_be.member.dto.request.LoginRequest;
import com.ucamp.gyeongjuma_be.member.dto.request.ReissueRequest;
import com.ucamp.gyeongjuma_be.member.dto.response.LoginResponse;
import com.ucamp.gyeongjuma_be.member.dto.response.LoginResult;
import com.ucamp.gyeongjuma_be.member.dto.response.MemberInfoResponse;
import com.ucamp.gyeongjuma_be.member.dto.response.NicknameCheckResponse;
import com.ucamp.gyeongjuma_be.member.dto.response.TokenResponse;
import com.ucamp.gyeongjuma_be.member.dto.response.TokenResult;
import com.ucamp.gyeongjuma_be.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final RefreshTokenCookieProvider refreshTokenCookieProvider;

    /**
     * 1. 소셜 로그인 (Google id_token 검증)
     * 리프레시 토큰은 응답 바디가 아니라 httpOnly 쿠키로 내려간다.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = memberService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookieProvider.create(result.refreshToken()).toString())
                .body(ApiResponse.success("로그인에 성공했습니다.", result.toResponse()));
    }

    /**
     * 1-1. 내 정보 조회 (앱 시작 시 토큰 유효성 확인용 — 인터셉터가 토큰을 검증하고,
     *      유효하면 사용자 정보를 반환한다. 토큰이 무효/만료면 401)
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberInfoResponse>> getMyInfo(
            @RequestAttribute(AuthInterceptor.MEMBER_ID_ATTRIBUTE) Long memberId) {
        MemberInfoResponse response = memberService.getMyInfo(memberId);
        return ResponseEntity.ok(ApiResponse.success("회원 정보 조회에 성공했습니다.", response));
    }

    /**
     * 2. 추가 정보 등록 (닉네임, 퀴즈 난이도, 언어)
     */
    @PatchMapping("/extra-info")
    public ResponseEntity<ApiResponse<MemberInfoResponse>> registerExtraInfo(
            @RequestAttribute(AuthInterceptor.MEMBER_ID_ATTRIBUTE) Long memberId,
            @Valid @RequestBody ExtraInfoRequest request) {
        MemberInfoResponse response = memberService.registerExtraInfo(memberId, request);
        return ResponseEntity.ok(ApiResponse.success("추가 정보가 등록되었습니다.", response));
    }

    /**
     * 3. 닉네임 중복 확인
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<ApiResponse<NicknameCheckResponse>> checkNickname(
            @RequestParam("nickname") String nickname) {
        NicknameCheckResponse response = memberService.checkNickname(nickname);
        String message = response.available() ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다.";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    /**
     * 4. 토큰 재발급
     * 리프레시 토큰을 httpOnly 쿠키에서 읽고, 새 리프레시 토큰도 쿠키로 다시 내려준다.
     */
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(
            @CookieValue(value = RefreshTokenCookieProvider.COOKIE_NAME, required = false) String cookieToken,
            @RequestBody(required = false) ReissueRequest request) {
        String refreshToken = resolveRefreshToken(cookieToken, request);
        TokenResult result = memberService.reissue(refreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookieProvider.create(result.refreshToken()).toString())
                .body(ApiResponse.success("토큰이 재발급되었습니다.",
                        TokenResponse.builder().accessToken(result.accessToken()).build()));
    }

    /**
     * 5. 로그아웃 (DB의 리프레시 토큰 무효화 + 쿠키 삭제)
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestAttribute(AuthInterceptor.MEMBER_ID_ATTRIBUTE) Long memberId) {
        memberService.logout(memberId);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookieProvider.clear().toString())
                .body(ApiResponse.success("로그아웃되었습니다."));
    }

    /**
     * 6. 회원 탈퇴 (soft delete + 쿠키 삭제)
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @RequestAttribute(AuthInterceptor.MEMBER_ID_ATTRIBUTE) Long memberId) {
        memberService.withdraw(memberId);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookieProvider.clear().toString())
                .body(ApiResponse.success("회원 탈퇴가 완료되었습니다."));
    }

    /**
     * 쿠키를 우선 사용하고, 없으면 요청 바디를 본다.
     * 바디 fallback은 프론트가 쿠키 방식으로 전환할 때까지만 유지하는 과도기 코드다.
     */
    private String resolveRefreshToken(String cookieToken, ReissueRequest request) {
        if (cookieToken != null && !cookieToken.isBlank()) {
            return cookieToken;
        }
        if (request != null && request.refreshToken() != null && !request.refreshToken().isBlank()) {
            return request.refreshToken();
        }
        throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
    }
}
