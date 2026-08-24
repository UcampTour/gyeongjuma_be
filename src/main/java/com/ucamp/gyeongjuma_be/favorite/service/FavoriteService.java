package com.ucamp.gyeongjuma_be.favorite.service;

import com.ucamp.gyeongjuma_be.favorite.dto.FavoriteResponse;

public interface FavoriteService {
    FavoriteResponse toggleFavorite(Long memberId, Long placeId);
}
