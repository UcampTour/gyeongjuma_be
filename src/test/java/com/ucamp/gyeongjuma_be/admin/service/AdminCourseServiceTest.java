package com.ucamp.gyeongjuma_be.admin.service;

import com.ucamp.gyeongjuma_be.admin.dto.request.CourseCreateRequest;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCourseDetailResponse;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCourseDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCourseListResponse;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCoursePlaceDto;
import com.ucamp.gyeongjuma_be.admin.repository.AdminCourseRepository;
import com.ucamp.gyeongjuma_be.common.exception.CustomException;
import com.ucamp.gyeongjuma_be.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminCourseServiceImpl.createCourse - 코스 등록")
class AdminCourseServiceTest {

    @Mock
    AdminCourseRepository adminCourseRepository;

    @InjectMocks
    AdminCourseServiceImpl adminCourseService;

    private CourseCreateRequest request(List<Long> placeIds) {
        return new CourseCreateRequest("경주 역사 탐방", "반나절 코스", "https://img/c.png", placeIds);
    }

    private void stubDetailLookup(Long courseId, int placeCnt) {
        AdminCourseDto course = new AdminCourseDto();
        course.setCourseId(courseId);
        course.setName("경주 역사 탐방");
        course.setDescription("반나절 코스");
        course.setThumbnailUrl("https://img/c.png");
        course.setPlaceCnt((long) placeCnt);
        course.setIsActive(true);
        course.setCreatedAt(LocalDateTime.now());
        given(adminCourseRepository.findCourseById(courseId)).willReturn(course);

        AdminCoursePlaceDto first = new AdminCoursePlaceDto();
        first.setPlaceId(10L);
        first.setSortOrder(1);
        AdminCoursePlaceDto second = new AdminCoursePlaceDto();
        second.setPlaceId(20L);
        second.setSortOrder(2);
        given(adminCourseRepository.findCoursePlaces(courseId)).willReturn(List.of(first, second));
    }

    @Test
    @DisplayName("정상 등록 시 place_cnt는 장소 개수로 자동 계산되고 상세를 반환한다")
    void createsCourse() {
        List<Long> placeIds = List.of(10L, 20L);
        given(adminCourseRepository.findActivePlaceIds(placeIds)).willReturn(List.of(10L, 20L));
        given(adminCourseRepository.findLastInsertedCourseId()).willReturn(7L);
        stubDetailLookup(7L, 2);

        AdminCourseDetailResponse response = adminCourseService.createCourse(request(placeIds));

        verify(adminCourseRepository).insertCourse(any(CourseCreateRequest.class), eq(2), any(LocalDateTime.class));
        verify(adminCourseRepository).insertCoursePlaces(7L, placeIds);

        assertThat(response.courseId()).isEqualTo(7L);
        assertThat(response.placeCnt()).isEqualTo(2L);
        assertThat(response.places()).extracting(AdminCoursePlaceDto::getSortOrder)
                .containsExactly(1, 2);
    }

    @Test
    @DisplayName("같은 장소를 중복해서 담으면 DUPLICATE_COURSE_PLACE")
    void rejectsDuplicatePlaceIds() {
        assertThatThrownBy(() -> adminCourseService.createCourse(request(List.of(10L, 20L, 10L))))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_COURSE_PLACE);

        // 중복 검사는 DB 조회 전에 끝나야 한다
        verify(adminCourseRepository, never()).findActivePlaceIds(any());
        verify(adminCourseRepository, never()).insertCourse(any(), anyInt(), any());
    }

    @Test
    @DisplayName("존재하지 않거나 비활성인 장소가 섞이면 PLACE_NOT_FOUND")
    void rejectsUnknownPlace() {
        List<Long> placeIds = List.of(10L, 999L);
        // 999는 없거나 is_active=FALSE라 조회 결과에서 빠진다
        given(adminCourseRepository.findActivePlaceIds(placeIds)).willReturn(List.of(10L));

        assertThatThrownBy(() -> adminCourseService.createCourse(request(placeIds)))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLACE_NOT_FOUND);

        verify(adminCourseRepository, never()).insertCourse(any(), anyInt(), any());
        verify(adminCourseRepository, never()).insertCoursePlaces(anyLong(), any());
    }

    @Test
    @DisplayName("등록 직후 코스를 못 찾으면 COURSE_NOT_FOUND")
    void throwsWhenCourseMissingAfterInsert() {
        List<Long> placeIds = List.of(10L);
        given(adminCourseRepository.findActivePlaceIds(placeIds)).willReturn(List.of(10L));
        given(adminCourseRepository.findLastInsertedCourseId()).willReturn(7L);
        given(adminCourseRepository.findCourseById(7L)).willReturn(null);

        assertThatThrownBy(() -> adminCourseService.createCourse(request(placeIds)))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_NOT_FOUND);
    }

    @Test
    @DisplayName("목록: totalPages는 올림 계산된다")
    void listCalculatesTotalPages() {
        given(adminCourseRepository.findCourses(null, null, 0, 20)).willReturn(List.of());
        given(adminCourseRepository.countCourses(null, null)).willReturn(41L);

        AdminCourseListResponse response = adminCourseService.getCourses(null, null, 0, 20);

        assertThat(response.totalCnt()).isEqualTo(41L);
        assertThat(response.totalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("목록: page/size로 offset이 계산된다")
    void listCalculatesOffset() {
        given(adminCourseRepository.findCourses("경주", true, 40, 20)).willReturn(List.of());
        given(adminCourseRepository.countCourses("경주", true)).willReturn(0L);

        adminCourseService.getCourses("경주", true, 2, 20);

        verify(adminCourseRepository).findCourses("경주", true, 40, 20);
    }

    @Test
    @DisplayName("상세: 없는 코스면 COURSE_NOT_FOUND")
    void detailThrowsWhenMissing() {
        given(adminCourseRepository.findCourseById(99L)).willReturn(null);

        assertThatThrownBy(() -> adminCourseService.getCourseDetail(99L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_NOT_FOUND);
    }

    @Test
    @DisplayName("삭제: 활성 코스는 소프트 삭제된다")
    void deletesActiveCourse() {
        AdminCourseDto course = new AdminCourseDto();
        course.setCourseId(7L);
        course.setIsActive(true);
        given(adminCourseRepository.findCourseById(7L)).willReturn(course);

        adminCourseService.deleteCourse(7L);

        verify(adminCourseRepository).softDeleteCourse(eq(7L), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("삭제: 이미 삭제된 코스면 INVALID_INPUT_VALUE")
    void rejectsDeletingInactiveCourse() {
        AdminCourseDto course = new AdminCourseDto();
        course.setCourseId(7L);
        course.setIsActive(false);
        given(adminCourseRepository.findCourseById(7L)).willReturn(course);

        assertThatThrownBy(() -> adminCourseService.deleteCourse(7L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);

        verify(adminCourseRepository, never()).softDeleteCourse(anyLong(), any());
    }

    @Test
    @DisplayName("복구: 이미 활성인 코스면 INVALID_INPUT_VALUE")
    void rejectsRestoringActiveCourse() {
        AdminCourseDto course = new AdminCourseDto();
        course.setCourseId(7L);
        course.setIsActive(true);
        given(adminCourseRepository.findCourseById(7L)).willReturn(course);

        assertThatThrownBy(() -> adminCourseService.restoreCourse(7L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);

        verify(adminCourseRepository, never()).restoreCourse(anyLong(), any());
    }

    @Test
    @DisplayName("created_at은 KST 기준으로 넘어간다")
    void passesKstTimestamp() {
        List<Long> placeIds = List.of(10L, 20L);
        given(adminCourseRepository.findActivePlaceIds(placeIds)).willReturn(List.of(10L, 20L));
        given(adminCourseRepository.findLastInsertedCourseId()).willReturn(7L);
        stubDetailLookup(7L, 2);

        org.mockito.ArgumentCaptor<LocalDateTime> captor =
                org.mockito.ArgumentCaptor.forClass(LocalDateTime.class);
        adminCourseService.createCourse(request(placeIds));
        verify(adminCourseRepository).insertCourse(any(), anyInt(), captor.capture());

        LocalDateTime kstNow = LocalDateTime.now(java.time.ZoneId.of("Asia/Seoul"));
        assertThat(captor.getValue()).isBetween(kstNow.minusMinutes(1), kstNow.plusMinutes(1));
        assertThat(captor.getValue()).isAfter(LocalDateTime.now(java.time.ZoneId.of("UTC")).plusHours(8));
    }
}
