package com.ucamp.gyeongjuma_be.member.controller;

import com.ucamp.gyeongjuma_be.auth.AuthInterceptor;
import com.ucamp.gyeongjuma_be.common.dto.ApiResponse;
import com.ucamp.gyeongjuma_be.member.dto.request.ExtraInfoRequest;
import com.ucamp.gyeongjuma_be.member.dto.request.LoginRequest;
import com.ucamp.gyeongjuma_be.member.dto.request.ReissueRequest;
import com.ucamp.gyeongjuma_be.member.dto.response.LoginResponse;
import com.ucamp.gyeongjuma_be.member.dto.response.MemberInfoResponse;
import com.ucamp.gyeongjuma_be.member.dto.response.NicknameCheckResponse;
import com.ucamp.gyeongjuma_be.member.dto.response.TokenResponse;
import com.ucamp.gyeongjuma_be.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    /**
     * 1. 소셜 로그인 (Google id_token 검증)
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = memberService.login(request);
        return ResponseEntity.ok(ApiResponse.success("로그인에 성공했습니다.", response));
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
     */
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(@Valid @RequestBody ReissueRequest request) {
        TokenResponse response = memberService.reissue(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success("토큰이 재발급되었습니다.", response));
    }

    /**
     * 5. 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestAttribute(AuthInterceptor.MEMBER_ID_ATTRIBUTE) Long memberId) {
        memberService.logout(memberId);
        return ResponseEntity.ok(ApiResponse.success("로그아웃되었습니다."));
    }

    /**
     * 6. 회원 탈퇴 (soft delete)
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @RequestAttribute(AuthInterceptor.MEMBER_ID_ATTRIBUTE) Long memberId) {
        memberService.withdraw(memberId);
        return ResponseEntity.ok(ApiResponse.success("회원 탈퇴가 완료되었습니다."));
    }
}
