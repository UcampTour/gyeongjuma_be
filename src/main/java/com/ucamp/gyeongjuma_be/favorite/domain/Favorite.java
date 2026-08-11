package com.ucamp.gyeongjuma_be.favorite.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Favorite {
    private Long favoriteId;
    private Long memberId;
    private Long placeId;

    public Favorite(Long memberId, Long placeId) {
        this.memberId = memberId;
        this.placeId = placeId;
    }
}
