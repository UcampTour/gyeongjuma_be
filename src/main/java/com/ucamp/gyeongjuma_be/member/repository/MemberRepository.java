package com.ucamp.gyeongjuma_be.member.repository;

import com.ucamp.gyeongjuma_be.member.domain.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface MemberRepository {

    Member findByProviderAndProviderId(@Param("provider") String provider,
                                       @Param("providerId") String providerId);

    Member findById(@Param("memberId") Long memberId);

    boolean existsByNickname(@Param("nickname") String nickname);

    int insertMember(Member member);

    int updateExtraInfo(@Param("memberId") Long memberId,
                        @Param("nickname") String nickname,
                        @Param("difficulty") String difficulty);

    int updateRefreshToken(@Param("memberId") Long memberId,
                           @Param("refreshToken") String refreshToken,
                           @Param("refreshTokenExpiredAt") LocalDateTime refreshTokenExpiredAt);

    int reactivateMember(@Param("memberId") Long memberId);

    int softDeleteById(@Param("memberId") Long memberId);
}
