package com.ucamp.gyeongjuma_be.quiz.dto.response;

import lombok.Data;

@Data
public class lockedPlaceQuizDto {

    private Long placeId;
    private String placeName;
    private String imageUrl;
}
