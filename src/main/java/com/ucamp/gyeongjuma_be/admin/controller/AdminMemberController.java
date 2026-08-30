package com.ucamp.gyeongjuma_be.admin.controller;

import com.ucamp.gyeongjuma_be.admin.dto.request.MemberUpdateAdminRequest;
import com.ucamp.gyeongjuma_be.admin.dto.request.PointAdjustRequest;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminMemberDetailDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminMemberListResponse;
import com.ucamp.gyeongjuma_be.admin.service.AdminMemberService;
import com.ucamp.gyeongjuma_be.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    /**
     * 1. 회원 목록 조회 (검색·필터·페이징)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<AdminMemberListResponse>> getMembers(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "isActive", required = false) Boolean isActive,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        AdminMemberListResponse response = adminMemberService.getMembers(keyword, role, isActive, page, size);
        return ResponseEntity.ok(ApiResponse.success("회원 목록 조회에 성공했습니다.", response));
    }

    /**
     * 2. 회원 상세 조회 (활동 집계 포함)
     */
    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResponse<AdminMemberDetailDto>> getMemberDetail(@PathVariable Long memberId) {
        AdminMemberDetailDto response = adminMemberService.getMemberDetail(memberId);
        return ResponseEntity.ok(ApiResponse.success("회원 상세 조회에 성공했습니다.", response));
    }

    /**
     * 3. 강제 탈퇴 (소프트 삭제)
     */
    @DeleteMapping("/{memberId}")
    public ResponseEntity<ApiResponse<Void>> forceWithdraw(@PathVariable Long memberId) {
        adminMemberService.forceWithdraw(memberId);
        return ResponseEntity.ok(ApiResponse.success("회원을 강제 탈퇴 처리했습니다."));
    }

    /**
     * 4. 회원 수정 — 닉네임 변경과 포인트 조정을 한 번에 처리한다.
     * 보내지 않은 항목은 그대로 유지된다.
     */
    @PatchMapping("/{memberId}")
    public ResponseEntity<ApiResponse<AdminMemberDetailDto>> updateMember(
            @PathVariable Long memberId,
            @Valid @RequestBody MemberUpdateAdminRequest request) {
        AdminMemberDetailDto response = adminMemberService.updateMember(memberId, request);
        return ResponseEntity.ok(ApiResponse.success("회원 정보를 수정했습니다.", response));
    }

    /**
     * 4. 포인트 조정 (증감분 전달, 이력 기록)
     */
    @PatchMapping("/{memberId}/point")
    public ResponseEntity<ApiResponse<AdminMemberDetailDto>> adjustPoint(
            @PathVariable Long memberId,
            @Valid @RequestBody PointAdjustRequest request) {
        AdminMemberDetailDto response = adminMemberService.adjustPoint(memberId, request);
        return ResponseEntity.ok(ApiResponse.success("포인트를 조정했습니다.", response));
    }
}
