package com.ucamp.gyeongjuma_be.admin.service;

import com.ucamp.gyeongjuma_be.admin.dto.request.CourseCreateRequest;
import com.ucamp.gyeongjuma_be.admin.dto.request.CourseUpsertRequest;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCourseDetailResponse;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCourseDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCourseItemDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCourseItemListResponse;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCourseListResponse;

public interface AdminCourseService {

    AdminCourseListResponse getCourses(String keyword, Boolean isActive, int page, int size);

    AdminCourseDetailResponse getCourseDetail(Long courseId);

    AdminCourseDetailResponse createCourse(CourseCreateRequest request);

    void deleteCourse(Long courseId);

    AdminCourseDto restoreCourse(Long courseId);

    // ── 코스 관리 화면용 (type / isUse / places / contents) ──

    AdminCourseItemListResponse getCourseItems(String keyword, Boolean isUse, int page, int size);

    AdminCourseItemDto getCourseItem(Long courseId);

    AdminCourseItemDto createCourseItem(CourseUpsertRequest request);

    AdminCourseItemDto updateCourseItem(Long courseId, CourseUpsertRequest request);
}
