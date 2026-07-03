package com.ucamp.gyeongjuma_be.quiz.dto.response;

import lombok.Data;

@Data
public class visitedPlaceQuizDto {

    private Long placeId;
    private String placeName;
    private Long totalQuizCnt;
    private Long correctQuizCnt;
    private String description;
    private String imageUrl;
}
