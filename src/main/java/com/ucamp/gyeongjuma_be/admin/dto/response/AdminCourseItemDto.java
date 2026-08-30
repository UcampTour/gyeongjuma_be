package com.ucamp.gyeongjuma_be.admin.dto.response;

import lombok.Data;

import java.util.List;

/** 코스 관리 화면용 — 코스 + 담긴 장소 + 언어별 이름·설명 */
@Data
public class AdminCourseItemDto {

    private Long id;
    /** WALK, DRIVE, BIKE, TRANSIT */
    private String type;
    /** 사용 여부 (course.is_active) */
    private Boolean isUse;
    private List<AdminCourseSimplePlaceDto> places;
    private List<AdminCourseContentDto> contents;
}
