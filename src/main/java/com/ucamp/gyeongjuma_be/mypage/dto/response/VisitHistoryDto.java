package com.ucamp.gyeongjuma_be.mypage.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VisitHistoryDto {

    private Long visitId;
    private Long placeId;
    private String placeName;
    private String address;
    private String imageUrl;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime visitedAt;
}
