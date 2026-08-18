package com.ucamp.gyeongjuma_be.admin.controller;

import com.ucamp.gyeongjuma_be.admin.dto.request.CourseCreateRequest;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCourseDetailResponse;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCourseDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCourseListResponse;
import com.ucamp.gyeongjuma_be.admin.service.AdminCourseService;
import com.ucamp.gyeongjuma_be.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
public class AdminCourseController {

    private final AdminCourseService adminCourseService;

    /**
     * 1. 코스 목록 (검색 + 페이징)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<AdminCourseListResponse>> getCourses(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "isActive", required = false) Boolean isActive,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        AdminCourseListResponse response = adminCourseService.getCourses(keyword, isActive, page, size);
        return ResponseEntity.ok(ApiResponse.success("코스 목록을 조회했습니다.", response));
    }

    /**
     * 2. 코스 상세 (포함 장소 순서대로)
     */
    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<AdminCourseDetailResponse>> getCourseDetail(
            @PathVariable Long courseId) {
        AdminCourseDetailResponse response = adminCourseService.getCourseDetail(courseId);
        return ResponseEntity.ok(ApiResponse.success("코스 상세를 조회했습니다.", response));
    }

    /**
     * 3. 코스 등록 (코스 + 포함 장소를 한 번에)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AdminCourseDetailResponse>> createCourse(
            @Valid @RequestBody CourseCreateRequest request) {
        AdminCourseDetailResponse response = adminCourseService.createCourse(request);
        return ResponseEntity.ok(ApiResponse.success("코스를 등록했습니다.", response));
    }

    /**
     * 4. 코스 삭제 (소프트 삭제 — member_course_state가 course_id를 참조해 물리 삭제 불가)
     */
    @DeleteMapping("/{courseId}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Long courseId) {
        adminCourseService.deleteCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success("코스를 삭제했습니다.", null));
    }

    /**
     * 5. 삭제된 코스 복구
     */
    @PatchMapping("/{courseId}/restore")
    public ResponseEntity<ApiResponse<AdminCourseDto>> restoreCourse(@PathVariable Long courseId) {
        AdminCourseDto response = adminCourseService.restoreCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success("코스를 복구했습니다.", response));
    }
}
