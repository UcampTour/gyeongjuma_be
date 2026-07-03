package com.ucamp.gyeongjuma_be.congestion.domain;

import com.ucamp.gyeongjuma_be.common.domain.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class Congestion extends BaseEntity {

    private Long congestionLogId;
    private String score;
    private LocalDate logDate;
    private Long placeId;

    @Builder
    public Congestion(Long congestionLogId, String score, LocalDate logDate, Long placeId) {
        this.congestionLogId = congestionLogId;
        this.score = score;
        this.logDate = logDate;
        this.placeId = placeId;
    }
}
