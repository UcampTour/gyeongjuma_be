package com.ucamp.gyeongjuma_be.visit.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Visit {
    private Long visitId;
    private Long memberId;
    private Long placeId;

    public Visit(Long memberId, Long placeId) {
        this.memberId = memberId;
        this.placeId = placeId;
    }
}
