package com.ucamp.gyeongjuma_be.mypage.repository;

import com.ucamp.gyeongjuma_be.mypage.dto.response.CourseProgressDto;
import com.ucamp.gyeongjuma_be.mypage.dto.response.StampDto;
import com.ucamp.gyeongjuma_be.mypage.dto.response.VisitHistoryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MyPageRepository {

    List<VisitHistoryDto> findVisitHistoryByMemberId(@Param("memberId") Long memberId);

    long countVisitedPlacesByMemberId(@Param("memberId") Long memberId);

    List<StampDto> findStampsByMemberId(@Param("memberId") Long memberId);

    List<CourseProgressDto> findCourseProgressByMemberId(@Param("memberId") Long memberId);

    /** 세트에 속한 활성 문항을 모두 푼 퀴즈 세트 수 */
    long countCompletedQuizSetsByMemberId(@Param("memberId") Long memberId);

    /** 코스에 담긴 활성 장소를 모두 방문한 코스 수 */
    long countCompletedCoursesByMemberId(@Param("memberId") Long memberId);
}
