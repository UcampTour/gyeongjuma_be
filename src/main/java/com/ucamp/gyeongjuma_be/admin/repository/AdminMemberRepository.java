package com.ucamp.gyeongjuma_be.admin.repository;

import com.ucamp.gyeongjuma_be.admin.dto.response.AdminMemberDetailDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminMemberDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AdminMemberRepository {

    /**
     * 권한 조회. member.role 컬럼이 필요하다 (sql/admin_role.sql 참고).
     */
    String findRoleById(@Param("memberId") Long memberId);

    List<AdminMemberDto> findMembers(@Param("keyword") String keyword,
                                     @Param("role") String role,
                                     @Param("isActive") Boolean isActive,
                                     @Param("offset") int offset,
                                     @Param("size") int size);

    long countMembers(@Param("keyword") String keyword,
                      @Param("role") String role,
                      @Param("isActive") Boolean isActive);

    AdminMemberDetailDto findMemberDetail(@Param("memberId") Long memberId);

    /** 마스터 관리자 계정 조회 (provider='ADMIN', provider_id='master') */
    AdminMemberDetailDto findMasterAdmin(@Param("provider") String provider,
                                         @Param("providerId") String providerId);

    int softDeleteMember(@Param("memberId") Long memberId,
                         @Param("deletedAt") LocalDateTime deletedAt);

    int updateNickname(@Param("memberId") Long memberId,
                       @Param("nickname") String nickname,
                       @Param("updatedAt") LocalDateTime updatedAt);

    boolean existsByNicknameExcludingMember(@Param("memberId") Long memberId,
                                            @Param("nickname") String nickname);

    int addPoint(@Param("memberId") Long memberId, @Param("amount") Long amount);

    int insertPointHistory(@Param("memberId") Long memberId,
                           @Param("amount") Long amount,
                           @Param("description") String description,
                           @Param("createdAt") LocalDateTime createdAt);
}
