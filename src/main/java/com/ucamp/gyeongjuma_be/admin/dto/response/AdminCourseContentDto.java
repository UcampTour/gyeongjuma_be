package com.ucamp.gyeongjuma_be.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/** MyBatis 조회 결과 — 코스의 언어별 이름·설명 */
@Data
public class AdminCourseContentDto {

    private Long courseContentId;
    private String language;
    private String courseName;
    private String description;

    /** 코스별로 묶기 위한 값이라 응답에는 내보내지 않는다 */
    @JsonIgnore
    private Long courseId;
}
