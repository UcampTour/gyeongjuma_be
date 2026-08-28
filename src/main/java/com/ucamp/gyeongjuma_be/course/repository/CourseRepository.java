package com.ucamp.gyeongjuma_be.course.repository;

import com.ucamp.gyeongjuma_be.course.dto.response.CourseDto;
import com.ucamp.gyeongjuma_be.course.dto.response.CoursePlaceDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseRepository {

    List<CourseDto> findCourses();

    CourseDto findCourseById(@Param("courseId") Long courseId);

    /** 코스에 담긴 관광지를 도는 순서대로 */
    List<CoursePlaceDto> findCoursePlaces(@Param("courseId") Long courseId);
}
