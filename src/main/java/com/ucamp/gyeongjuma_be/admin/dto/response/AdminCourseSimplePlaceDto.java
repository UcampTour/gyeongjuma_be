package com.ucamp.gyeongjuma_be.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/** MyBatis 조회 결과 — 코스에 담긴 장소 (관리 화면은 id·name만 쓴다) */
@Data
public class AdminCourseSimplePlaceDto {

    private Long id;
    private String name;

    @JsonIgnore
    private Long courseId;
    /** 순서 유지용. 응답에는 배열 순서로 표현된다 */
    @JsonIgnore
    private Integer sortOrder;
}
