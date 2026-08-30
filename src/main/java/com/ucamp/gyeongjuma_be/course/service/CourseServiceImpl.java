package com.ucamp.gyeongjuma_be.course.service;

import com.ucamp.gyeongjuma_be.common.exception.CustomException;
import com.ucamp.gyeongjuma_be.common.exception.ErrorCode;
import com.ucamp.gyeongjuma_be.course.dto.response.CourseDetailResponse;
import com.ucamp.gyeongjuma_be.course.dto.response.CourseDto;
import com.ucamp.gyeongjuma_be.course.dto.response.CourseListResponse;
import com.ucamp.gyeongjuma_be.course.dto.response.CoursePlaceDto;
import com.ucamp.gyeongjuma_be.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 코스 조회 화면용. 회원별 진행 상황은 다루지 않는다 —
 * 진행률·완주 여부는 마이페이지(GET /api/mypage/courses)가 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    @Override
    public CourseListResponse getCourses(Long memberId) {
        List<CourseDto> courses = courseRepository.findCourses(memberId);

        return CourseListResponse.builder()
                .totalCnt(courses.size())
                .courses(courses)
                .build();
    }

    @Override
    public CourseDetailResponse getCourseDetail(Long memberId, Long courseId) {
        CourseDto course = courseRepository.findCourseById(memberId, courseId);
        if (course == null) {
            throw new CustomException(ErrorCode.COURSE_NOT_FOUND);
        }
        List<CoursePlaceDto> courseList = courseRepository.findCoursePlaces(courseId);

        return CourseDetailResponse.builder()
                .courseId(course.getCourseId())
                .title(course.getTitle())
                .description(course.getDescription())
                .type(course.getType())
                .thumbnailUrl(course.getThumbnailUrl())
                .placeCnt(course.getPlaceCnt())
                .courseList(courseList)
                .build();
    }
}
