package com.ucamp.gyeongjuma_be.mypage.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StampDto {

    private Long stampId;
    private String stampName;
    private String description;
    private Long requirement;
    private Long placeId;
    private String placeName;
    private Boolean isAcquired;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime acquiredAt;
}
