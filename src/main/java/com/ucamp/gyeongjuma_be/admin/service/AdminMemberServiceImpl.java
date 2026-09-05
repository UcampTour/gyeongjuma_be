package com.ucamp.gyeongjuma_be.admin.service;

import com.ucamp.gyeongjuma_be.admin.dto.request.MemberUpdateAdminRequest;
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
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberServiceImpl implements AdminMemberService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    /** 밀리초까지 넣는다 — 초 단위로는 연속 조정이 같은 초에 겹쳐 유니크 제약에 걸린다 */
    private static final DateTimeFormatter DESCRIPTION_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    /** point_history.description 컬럼 길이 */
    private static final int DESCRIPTION_MAX_LENGTH = 100;

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

        LocalDateTime now = LocalDateTime.now(KST);
        adminMemberRepository.addPoint(memberId, request.amount());
        adminMemberRepository.insertPointHistory(memberId, request.amount(),
                buildDescription(request.reasonOrDefault(), now), now);

        return adminMemberRepository.findMemberDetail(memberId);
    }

    /**
     * 관리자 회원 수정 — 닉네임과 포인트를 한 트랜잭션에서 처리한다.
     * 보내지 않은 항목은 건드리지 않으며, 포인트는 증감분(pointAmount)으로 받는다.
     */
    @Override
    @Transactional
    public AdminMemberDetailDto updateMember(Long memberId, MemberUpdateAdminRequest request) {
        AdminMemberDetailDto member = getExistingMember(memberId);
        if (Boolean.FALSE.equals(member.getIsActive())) {
            throw new CustomException(ErrorCode.ALREADY_WITHDRAWN_MEMBER);
        }
        if (request.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        LocalDateTime now = LocalDateTime.now(KST);

        String nickname = request.nickname();
        if (nickname != null && !nickname.equals(member.getNickname())) {
            // 탈퇴 회원은 닉네임을 반납하므로 활성 회원 중에서만 중복을 본다
            if (adminMemberRepository.existsByNicknameExcludingMember(memberId, nickname)) {
                throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
            }
            adminMemberRepository.updateNickname(memberId, nickname, now);
        }

        if (request.hasPointChange()) {
            long current = member.getPoint() == null ? 0L : member.getPoint();
            if (current + request.pointAmount() < 0) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
            }
            adminMemberRepository.addPoint(memberId, request.pointAmount());
            adminMemberRepository.insertPointHistory(memberId, request.pointAmount(),
                    buildDescription(request.reasonOrDefault(), now), now);
        }

        return adminMemberRepository.findMemberDetail(memberId);
    }

    /**
     * 이력 사유에 조정 시각을 덧붙인다.
     * point_history에 (member_id, description) 유니크가 걸려 있어 사유만으로는
     * 같은 회원을 두 번 조정할 수 없다 — 시각을 붙여 매 조정이 고유해지게 한다.
     * description 컬럼이 varchar(100)이라 넘치면 사유 쪽을 잘라낸다 (시각은 항상 보존).
     */
    private String buildDescription(String reason, LocalDateTime at) {
        String suffix = " (" + at.format(DESCRIPTION_TIME) + ")";
        int room = DESCRIPTION_MAX_LENGTH - suffix.length();
        String trimmed = reason.length() > room ? reason.substring(0, room) : reason;
        return trimmed + suffix;
    }

    private AdminMemberDetailDto getExistingMember(Long memberId) {
        AdminMemberDetailDto member = adminMemberRepository.findMemberDetail(memberId);
        if (member == null) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }
        return member;
    }
}
