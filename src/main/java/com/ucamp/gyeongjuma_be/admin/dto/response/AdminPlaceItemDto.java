package com.ucamp.gyeongjuma_be.admin.dto.response;

import lombok.Data;

import java.util.List;

/** 관광지 관리 화면용 — 관광지 + 언어·난이도별 해설 목록 */
@Data
public class AdminPlaceItemDto {

    private Long placeId;
    private String placeName;
    private String category;
    private Boolean isActive;
    private String language;
    private List<AdminPlaceContentDto> contents;
}
