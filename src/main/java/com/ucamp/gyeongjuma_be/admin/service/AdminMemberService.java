package com.ucamp.gyeongjuma_be.admin.service;

import com.ucamp.gyeongjuma_be.admin.dto.request.MemberUpdateAdminRequest;
import com.ucamp.gyeongjuma_be.admin.dto.request.PointAdjustRequest;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminMemberDetailDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminMemberListResponse;

public interface AdminMemberService {

    AdminMemberListResponse getMembers(String keyword, String role, Boolean isActive, int page, int size);

    AdminMemberDetailDto getMemberDetail(Long memberId);

    void forceWithdraw(Long memberId);

    AdminMemberDetailDto adjustPoint(Long memberId, PointAdjustRequest request);

    AdminMemberDetailDto updateMember(Long memberId, MemberUpdateAdminRequest request);
}
