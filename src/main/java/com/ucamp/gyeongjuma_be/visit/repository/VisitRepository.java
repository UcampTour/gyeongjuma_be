package com.ucamp.gyeongjuma_be.visit.repository;

import com.ucamp.gyeongjuma_be.visit.domain.Visit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VisitRepository {
    List<Long> findRecentPlaceIdsByMemberId(@Param("memberId") Long memberId);

    boolean existsByMemberIdAndPlaceId(@Param("memberId") Long memberId, @Param("placeId") Long placeId);

    int save(Visit visit);

    /** 방금 저장한 방문을 같은 관광지의 다른 언어판에도 남긴다 */
    int saveGroupSiblings(@Param("visitId") Long visitId);
}
