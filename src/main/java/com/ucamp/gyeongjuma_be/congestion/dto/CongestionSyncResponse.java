package com.ucamp.gyeongjuma_be.congestion.dto;

import com.ucamp.gyeongjuma_be.congestion.domain.Congestion;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class CongestionSyncResponse {

    private Long placeId;
    private LocalDate logDate;
    private String score;

    public static CongestionSyncResponse from(Congestion congestion) {
        return CongestionSyncResponse.builder()
                .placeId(congestion.getPlaceId())
                .logDate(congestion.getLogDate())
                .score(congestion.getScore())
                .build();
    }
}
