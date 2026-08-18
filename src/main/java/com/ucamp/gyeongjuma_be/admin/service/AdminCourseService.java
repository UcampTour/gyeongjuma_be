package com.ucamp.gyeongjuma_be.admin.service;

import com.ucamp.gyeongjuma_be.admin.dto.request.CourseCreateRequest;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCourseDetailResponse;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCourseDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminCourseListResponse;

public interface AdminCourseService {

    AdminCourseListResponse getCourses(String keyword, Boolean isActive, int page, int size);

    AdminCourseDetailResponse getCourseDetail(Long courseId);

    AdminCourseDetailResponse createCourse(CourseCreateRequest request);

    void deleteCourse(Long courseId);

    AdminCourseDto restoreCourse(Long courseId);
}
