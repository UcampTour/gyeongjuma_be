package com.ucamp.gyeongjuma_be.admin.service;

import com.ucamp.gyeongjuma_be.admin.dto.request.CourseCreateRequest;
import com.ucamp.gyeongjuma_be.admin.dto.request.CourseUpsertRequest;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCourseDetailResponse;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCourseDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCourseContentDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCourseItemDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCourseItemListResponse;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCourseListResponse;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCourseSimplePlaceDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCoursePlaceDto;
import com.ucamp.gyeongjuma_be.admin.repository.AdminCourseRepository;
import com.ucamp.gyeongjuma_be.common.exception.CustomException;
import com.ucamp.gyeongjuma_be.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    // ── 코스 관리 화면용 ──

    @Override
    public AdminCourseItemListResponse getCourseItems(String keyword, Boolean isUse, int page, int size) {
        int offset = page * size;
        List<AdminCourseItemDto> courses = adminCourseRepository.findCourseItems(keyword, isUse, offset, size);
        attachDetails(courses);
        long totalCnt = adminCourseRepository.countCourseItems(keyword, isUse);

        return AdminCourseItemListResponse.builder()
                .totalCnt(totalCnt)
                .page(page)
                .size(size)
                .totalPages((int) Math.ceil((double) totalCnt / size))
                .courses(courses)
                .build();
    }

    @Override
    public AdminCourseItemDto getCourseItem(Long courseId) {
        AdminCourseItemDto course = adminCourseRepository.findCourseItemById(courseId);
        if (course == null) {
            throw new CustomException(ErrorCode.COURSE_NOT_FOUND);
        }
        attachDetails(List.of(course));
        return course;
    }

    @Override
    @Transactional
    public AdminCourseItemDto createCourseItem(CourseUpsertRequest request) {
        validateUpsert(request);

        LocalDateTime now = LocalDateTime.now(KST);
        CourseUpsertRequest.CourseContent base = baseContent(request);

        // course.name/description은 course_content의 기본 언어(ko 우선) 값을 복사해 둔다.
        // NOT NULL 컬럼이고, 언어를 안 따지는 화면(사용자 코스 목록)이 이 값을 쓴다.
        adminCourseRepository.insertCourseShell(base.courseName(), base.description(),
                request.typeOrDefault(), request.placeIds().size(), request.isUseOrDefault(), now);
        Long courseId = adminCourseRepository.findLastInsertedCourseId();

        adminCourseRepository.insertCoursePlaces(courseId, request.placeIds());
        for (CourseUpsertRequest.CourseContent content : request.contents()) {
            adminCourseRepository.insertCourseContent(courseId, content.normalizedLanguage(),
                    content.courseName(), content.description());
        }

        return getCourseItem(courseId);
    }

    /**
     * 코스 수정. 장소 목록과 언어별 콘텐츠는 통째로 교체한다 —
     * 부분 갱신보다 규칙이 단순하고, 순서 재배치·언어 삭제가 한 번에 처리된다.
     */
    @Override
    @Transactional
    public AdminCourseItemDto updateCourseItem(Long courseId, CourseUpsertRequest request) {
        getCourseItem(courseId);
        validateUpsert(request);

        LocalDateTime now = LocalDateTime.now(KST);
        CourseUpsertRequest.CourseContent base = baseContent(request);

        adminCourseRepository.updateCourseShell(courseId, base.courseName(), base.description(),
                request.typeOrDefault(), request.placeIds().size(), request.isUseOrDefault(), now);

        adminCourseRepository.deleteCoursePlaces(courseId);
        adminCourseRepository.insertCoursePlaces(courseId, request.placeIds());

        adminCourseRepository.deleteCourseContents(courseId);
        for (CourseUpsertRequest.CourseContent content : request.contents()) {
            adminCourseRepository.insertCourseContent(courseId, content.normalizedLanguage(),
                    content.courseName(), content.description());
        }

        return getCourseItem(courseId);
    }

    private void validateUpsert(CourseUpsertRequest request) {
        List<Long> placeIds = request.placeIds();
        Set<Long> distinctPlaces = new HashSet<>(placeIds);
        if (distinctPlaces.size() != placeIds.size()) {
            throw new CustomException(ErrorCode.DUPLICATE_COURSE_PLACE);
        }
        if (adminCourseRepository.findActivePlaceIds(placeIds).size() != distinctPlaces.size()) {
            throw new CustomException(ErrorCode.PLACE_NOT_FOUND);
        }

        // 같은 언어를 두 번 보내면 course_content의 (course_id, language) 유니크에 걸린다
        Set<String> languages = new LinkedHashSet<>();
        for (CourseUpsertRequest.CourseContent content : request.contents()) {
            if (!languages.add(content.normalizedLanguage())) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }
    }

    /** course.name에 복사할 기준 콘텐츠 — ko가 있으면 ko, 없으면 첫 번째 */
    private CourseUpsertRequest.CourseContent baseContent(CourseUpsertRequest request) {
        return request.contents().stream()
                .filter(c -> "ko".equals(c.normalizedLanguage()))
                .findFirst()
                .orElse(request.contents().get(0));
    }

    /** 코스 목록에 장소·콘텐츠를 한 번의 조회로 묶어 넣는다 (N+1 회피) */
    private void attachDetails(List<AdminCourseItemDto> courses) {
        if (courses.isEmpty()) {
            return;
        }
        List<Long> courseIds = courses.stream().map(AdminCourseItemDto::getId).toList();

        Map<Long, List<AdminCourseSimplePlaceDto>> placesByCourse =
                adminCourseRepository.findSimplePlacesByCourseIds(courseIds).stream()
                        .collect(Collectors.groupingBy(AdminCourseSimplePlaceDto::getCourseId));
        Map<Long, List<AdminCourseContentDto>> contentsByCourse =
                adminCourseRepository.findContentsByCourseIds(courseIds).stream()
                        .collect(Collectors.groupingBy(AdminCourseContentDto::getCourseId));

        courses.forEach(course -> {
            course.setPlaces(placesByCourse.getOrDefault(course.getId(), Collections.emptyList()));
            course.setContents(contentsByCourse.getOrDefault(course.getId(), Collections.emptyList()));
        });
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
