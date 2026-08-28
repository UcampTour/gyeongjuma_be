package com.ucamp.gyeongjuma_be.course.service;

import com.ucamp.gyeongjuma_be.course.dto.response.CourseDetailResponse;
import com.ucamp.gyeongjuma_be.course.dto.response.CourseListResponse;

public interface CourseService {

    CourseListResponse getCourses();

    CourseDetailResponse getCourseDetail(Long courseId);
}
