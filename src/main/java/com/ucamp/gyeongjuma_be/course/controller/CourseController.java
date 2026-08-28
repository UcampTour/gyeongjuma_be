package com.ucamp.gyeongjuma_be.course.controller;

import com.ucamp.gyeongjuma_be.common.dto.ApiResponse;
import com.ucamp.gyeongjuma_be.course.dto.response.CourseDetailResponse;
import com.ucamp.gyeongjuma_be.course.dto.response.CourseListResponse;
import com.ucamp.gyeongjuma_be.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * 1. 코스 목록 (코스 조회 화면)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CourseListResponse>> getCourses() {
        CourseListResponse response = courseService.getCourses();
        return ResponseEntity.ok(ApiResponse.success("코스 목록 조회에 성공했습니다.", response));
    }

    /**
     * 2. 코스 상세 — 포함 관광지를 도는 순서대로
     */
    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseDetailResponse>> getCourseDetail(
            @PathVariable Long courseId) {
        CourseDetailResponse response = courseService.getCourseDetail(courseId);
        return ResponseEntity.ok(ApiResponse.success("코스 상세 조회에 성공했습니다.", response));
    }
}
