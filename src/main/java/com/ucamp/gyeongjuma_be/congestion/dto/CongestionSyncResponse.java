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
                .score(toCongestionLevel(congestion.getScore()))
                .build();
    }

    private static String toCongestionLevel(String score) {
        if (score == null || score.isBlank()) {
            return null;
        }

        try {
            double value = Double.parseDouble(score.trim());

            if (value >= 0 && value <= 30) {
                return "\uC5EC\uC720";
            }
            if (value >= 31 && value <= 60) {
                return "\uBCF4\uD1B5";
            }
            if (value >= 61 && value <= 100) {
                return "\uD63C\uC7A1";
            }

            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
