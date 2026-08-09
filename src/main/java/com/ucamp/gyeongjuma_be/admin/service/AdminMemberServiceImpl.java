package com.ucamp.gyeongjuma_be.admin.service;

import com.ucamp.gyeongjuma_be.admin.dto.request.PointAdjustRequest;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminMemberDetailDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminMemberDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminMemberListResponse;
import com.ucamp.gyeongjuma_be.admin.repository.AdminMemberRepository;
import com.ucamp.gyeongjuma_be.common.exception.CustomException;
import com.ucamp.gyeongjuma_be.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberServiceImpl implements AdminMemberService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final AdminMemberRepository adminMemberRepository;

    @Override
    public AdminMemberListResponse getMembers(String keyword, String role, Boolean isActive, int page, int size) {
        int offset = page * size;
        List<AdminMemberDto> members = adminMemberRepository.findMembers(keyword, role, isActive, offset, size);
        long totalCnt = adminMemberRepository.countMembers(keyword, role, isActive);

        return AdminMemberListResponse.builder()
                .totalCnt(totalCnt)
                .page(page)
                .size(size)
                .totalPages((int) Math.ceil((double) totalCnt / size))
                .members(members)
                .build();
    }

    @Override
    public AdminMemberDetailDto getMemberDetail(Long memberId) {
        return getExistingMember(memberId);
    }

    @Override
    @Transactional
    public void forceWithdraw(Long memberId) {
        AdminMemberDetailDto member = getExistingMember(memberId);
        if (Boolean.FALSE.equals(member.getIsActive())) {
            throw new CustomException(ErrorCode.ALREADY_WITHDRAWN_MEMBER);
        }
        adminMemberRepository.softDeleteMember(memberId, LocalDateTime.now(KST));
    }

    @Override
    @Transactional
    public AdminMemberDetailDto adjustPoint(Long memberId, PointAdjustRequest request) {
        AdminMemberDetailDto member = getExistingMember(memberId);
        if (Boolean.FALSE.equals(member.getIsActive())) {
            throw new CustomException(ErrorCode.ALREADY_WITHDRAWN_MEMBER);
        }

        long current = member.getPoint() == null ? 0L : member.getPoint();
        if (current + request.amount() < 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        adminMemberRepository.addPoint(memberId, request.amount());
        adminMemberRepository.insertPointHistory(memberId, request.amount(),
                request.reasonOrDefault(), LocalDateTime.now(KST));

        return adminMemberRepository.findMemberDetail(memberId);
    }

    private AdminMemberDetailDto getExistingMember(Long memberId) {
        AdminMemberDetailDto member = adminMemberRepository.findMemberDetail(memberId);
        if (member == null) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }
        return member;
    }
}
