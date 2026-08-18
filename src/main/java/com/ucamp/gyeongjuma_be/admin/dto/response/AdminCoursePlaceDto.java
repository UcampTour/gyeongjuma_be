package com.ucamp.gyeongjuma_be.admin.dto.response;

import lombok.Data;

/** MyBatis 조회 결과 — record가 아니라 @Data 클래스여야 매핑된다 */
@Data
public class AdminCoursePlaceDto {

    private Long coursePlaceId;
    private Long placeId;
    private String placeName;
    private String address;
    private String imageUrl;
    private Integer sortOrder;
}
