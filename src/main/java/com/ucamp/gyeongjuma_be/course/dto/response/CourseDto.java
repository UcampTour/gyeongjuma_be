package com.ucamp.gyeongjuma_be.course.dto.response;

import lombok.Data;

/** MyBatis 조회 결과 — 코스 목록 행 */
@Data
public class CourseDto {

    private Long courseId;
    private String title;
    private String description;
    /** WALK, DRIVE, BIKE, TRANSIT */
    private String type;
    private String thumbnailUrl;
    private Long placeCnt;
}
