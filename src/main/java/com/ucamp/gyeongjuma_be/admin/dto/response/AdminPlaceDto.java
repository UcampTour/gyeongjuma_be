package com.ucamp.gyeongjuma_be.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminPlaceDto {

    private Long placeId;
    private String placeName;
    private String add1;
    private String add2;
    private String tel;
    private Long contentTypeId;
    private Double mapX;
    private Double mapY;
    private String firstImage;
    private Long radiusMeters;
    private Boolean isActive;

    // 삭제 시 영향 범위 파악용
    private Long visitCnt;
    private Long quizSetCnt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deletedAt;
}
