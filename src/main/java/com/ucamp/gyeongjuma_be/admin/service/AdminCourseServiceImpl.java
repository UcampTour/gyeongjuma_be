package com.ucamp.gyeongjuma_be.admin.service;

import com.ucamp.gyeongjuma_be.admin.dto.request.CourseCreateRequest;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCourseDetailResponse;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCourseDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCourseListResponse;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCoursePlaceDto;
import com.ucamp.gyeongjuma_be.admin.repository.AdminCourseRepository;
import com.ucamp.gyeongjuma_be.common.exception.CustomException;
import com.ucamp.gyeongjuma_be.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCourseServiceImpl implements AdminCourseService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final AdminCourseRepository adminCourseRepository;

    @Override
    public AdminCourseListResponse getCourses(String keyword, Boolean isActive, int page, int size) {
        int offset = page * size;
        List<AdminCourseDto> courses = adminCourseRepository.findCourses(keyword, isActive, offset, size);
        long totalCnt = adminCourseRepository.countCourses(keyword, isActive);

        return AdminCourseListResponse.builder()
                .totalCnt(totalCnt)
                .page(page)
                .size(size)
                .totalPages((int) Math.ceil((double) totalCnt / size))
                .courses(courses)
                .build();
    }

    @Override
    public AdminCourseDetailResponse getCourseDetail(Long courseId) {
        return toDetailResponse(courseId);
    }

    /**
     * 코스 등록. course 1건 + course_place N건을 한 트랜잭션으로 넣는다.
     * placeIds의 나열 순서가 sort_order 1..N이 되고, place_cnt는 목록 크기로 자동 계산한다.
     */
    @Override
    @Transactional
    public AdminCourseDetailResponse createCourse(CourseCreateRequest request) {
        List<Long> placeIds = request.placeIds();

        // 같은 장소를 두 번 담으면 sort_order가 모호해지고 진행률 계산도 어긋난다
        Set<Long> distinct = new HashSet<>(placeIds);
        if (distinct.size() != placeIds.size()) {
            throw new CustomException(ErrorCode.DUPLICATE_COURSE_PLACE);
        }

        // 존재하지 않거나 비활성화된 장소가 섞여 있으면 등록 자체를 막는다
        List<Long> activeIds = adminCourseRepository.findActivePlaceIds(placeIds);
        if (activeIds.size() != distinct.size()) {
            throw new CustomException(ErrorCode.PLACE_NOT_FOUND);
        }

        adminCourseRepository.insertCourse(request, placeIds.size(), LocalDateTime.now(KST));
        Long courseId = adminCourseRepository.findLastInsertedCourseId();
        adminCourseRepository.insertCoursePlaces(courseId, placeIds);

        return toDetailResponse(courseId);
    }

    /**
     * 소프트 삭제. member_course_state가 course_id를 FK로 참조하고 있어 물리 삭제는 불가능하다.
     */
    @Override
    @Transactional
    public void deleteCourse(Long courseId) {
        AdminCourseDto course = getExistingCourse(courseId);
        if (Boolean.FALSE.equals(course.getIsActive())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        adminCourseRepository.softDeleteCourse(courseId, LocalDateTime.now(KST));
    }

    @Override
    @Transactional
    public AdminCourseDto restoreCourse(Long courseId) {
        AdminCourseDto course = getExistingCourse(courseId);
        if (Boolean.TRUE.equals(course.getIsActive())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        adminCourseRepository.restoreCourse(courseId, LocalDateTime.now(KST));
        return adminCourseRepository.findCourseById(courseId);
    }

    private AdminCourseDto getExistingCourse(Long courseId) {
        AdminCourseDto course = adminCourseRepository.findCourseById(courseId);
        if (course == null) {
            throw new CustomException(ErrorCode.COURSE_NOT_FOUND);
        }
        return course;
    }

    private AdminCourseDetailResponse toDetailResponse(Long courseId) {
        AdminCourseDto course = getExistingCourse(courseId);
        List<AdminCoursePlaceDto> places = adminCourseRepository.findCoursePlaces(courseId);

        return AdminCourseDetailResponse.builder()
                .courseId(course.getCourseId())
                .name(course.getName())
                .description(course.getDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .type(course.getType())
                .placeCnt(course.getPlaceCnt())
                .isActive(course.getIsActive())
                .createdAt(course.getCreatedAt())
                .places(places)
                .build();
    }
}
