package com.ucamp.gyeongjuma_be.admin.repository;

import com.ucamp.gyeongjuma_be.admin.dto.request.CourseCreateRequest;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCourseContentDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCourseDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCourseItemDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCourseSimplePlaceDto;
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

    // ── 코스 관리 화면용 ──

    List<AdminCourseItemDto> findCourseItems(@Param("keyword") String keyword,
                                             @Param("isUse") Boolean isUse,
                                             @Param("offset") int offset,
                                             @Param("size") int size);

    long countCourseItems(@Param("keyword") String keyword,
                          @Param("isUse") Boolean isUse);

    AdminCourseItemDto findCourseItemById(@Param("courseId") Long courseId);

    List<AdminCourseSimplePlaceDto> findSimplePlacesByCourseIds(@Param("courseIds") List<Long> courseIds);

    List<AdminCourseContentDto> findContentsByCourseIds(@Param("courseIds") List<Long> courseIds);

    int insertCourseShell(@Param("name") String name,
                          @Param("description") String description,
                          @Param("type") String type,
                          @Param("placeCnt") int placeCnt,
                          @Param("isUse") boolean isUse,
                          @Param("createdAt") LocalDateTime createdAt);

    int updateCourseShell(@Param("courseId") Long courseId,
                          @Param("name") String name,
                          @Param("description") String description,
                          @Param("type") String type,
                          @Param("placeCnt") int placeCnt,
                          @Param("isUse") boolean isUse,
                          @Param("updatedAt") LocalDateTime updatedAt);

    int insertCourseContent(@Param("courseId") Long courseId,
                            @Param("language") String language,
                            @Param("courseName") String courseName,
                            @Param("description") String description);

    int deleteCourseContents(@Param("courseId") Long courseId);

    int deleteCoursePlaces(@Param("courseId") Long courseId);

    int softDeleteCourse(@Param("courseId") Long courseId,
                         @Param("deletedAt") LocalDateTime deletedAt);

    int restoreCourse(@Param("courseId") Long courseId,
                      @Param("updatedAt") LocalDateTime updatedAt);
}
