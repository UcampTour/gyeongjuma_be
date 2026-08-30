package com.ucamp.gyeongjuma_be.course.dto.response;

import lombok.Data;

/** MyBatis 조회 결과 — 코스에 담긴 관광지 (도는 순서 포함) */
@Data
public class CoursePlaceDto {

    /** 코스 안에서의 순서 (1부터) */
    private Integer courseSeqNo;
    private Long placeId;
    private String placeName;
    private String image;
    private String address;
    private Double latitude;
    private Double longitude;
}
