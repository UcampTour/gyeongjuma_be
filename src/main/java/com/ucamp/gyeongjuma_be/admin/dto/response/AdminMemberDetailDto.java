package com.ucamp.gyeongjuma_be.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminMemberDetailDto {

    private Long memberId;
    private String nickname;
    private String provider;
    private String providerId;
    private String profileImage;
    private String role;
    private String difficulty;
    private String locale;
    private Long point;
    private Long distance;
    private Boolean isActive;

    // 활동 집계
    private Long visitCnt;
    private Long stampCnt;
    private Long quizAnswerCnt;
    private Long quizCorrectCnt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deletedAt;
}
