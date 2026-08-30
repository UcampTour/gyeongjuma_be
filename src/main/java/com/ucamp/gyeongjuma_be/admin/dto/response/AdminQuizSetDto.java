package com.ucamp.gyeongjuma_be.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminQuizSetDto {

    private Long placeQuizInfoId;
    private Long placeId;
    private String placeName;
    private String title;
    private String description;
    private String difficulty;
    private String language;
    /** 이 세트가 번역본이면 원본 세트 ID, 원본이면 null */
    private Long originInfoId;
    private Long questionCnt;
    private Boolean isActive;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deletedAt;
}
