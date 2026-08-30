package com.ucamp.gyeongjuma_be.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/** MyBatis 조회 결과 — 관광지 해설 (언어·난이도별) */
@Data
public class AdminPlaceContentDto {

    private Long placeContentId;
    private String difficulty;
    private String language;
    private String description;

    /** 관광지별로 묶기 위한 값이라 응답에는 내보내지 않는다 */
    @JsonIgnore
    private Long placeId;
}
