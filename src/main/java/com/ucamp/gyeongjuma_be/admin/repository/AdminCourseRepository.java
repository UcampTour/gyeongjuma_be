package com.ucamp.gyeongjuma_be.admin.repository;

import com.ucamp.gyeongjuma_be.admin.dto.request.CourseCreateRequest;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCourseDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCoursePlaceDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AdminCourseRepository {

    List<AdminCourseDto> findCourses(@Param("keyword") String keyword,
                                     @Param("isActive") Boolean isActive,
                                     @Param("offset") int offset,
                                     @Param("size") int size);

    long countCourses(@Param("keyword") String keyword,
                      @Param("isActive") Boolean isActive);

    int insertCourse(@Param("request") CourseCreateRequest request,
                     @Param("placeCnt") int placeCnt,
                     @Param("createdAt") LocalDateTime createdAt);

    Long findLastInsertedCourseId();

    /** placeIds의 나열 순서대로 sort_order 1..N을 부여해 일괄 INSERT */
    int insertCoursePlaces(@Param("courseId") Long courseId,
                           @Param("placeIds") List<Long> placeIds);

    AdminCourseDto findCourseById(@Param("courseId") Long courseId);

    List<AdminCoursePlaceDto> findCoursePlaces(@Param("courseId") Long courseId);

    /** 넘어온 ID 중 실제로 존재하고 활성화된 장소의 ID만 반환 */
    List<Long> findActivePlaceIds(@Param("placeIds") List<Long> placeIds);

    int softDeleteCourse(@Param("courseId") Long courseId,
                         @Param("deletedAt") LocalDateTime deletedAt);

    int restoreCourse(@Param("courseId") Long courseId,
                      @Param("updatedAt") LocalDateTime updatedAt);
}
