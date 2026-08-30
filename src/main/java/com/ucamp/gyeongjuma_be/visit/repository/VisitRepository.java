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
}
