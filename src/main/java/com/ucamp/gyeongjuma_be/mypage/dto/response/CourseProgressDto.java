package com.ucamp.gyeongjuma_be.mypage.dto.response;

import lombok.Data;

@Data
public class CourseProgressDto {

    private Long courseId;
    private String courseName;
    private Long placeCnt;
    private Long visitedCnt;
    private Double progressRate;
    private Boolean isCompleted;
}
