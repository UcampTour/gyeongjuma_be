package com.ucamp.gyeongjuma_be.favorite.service;

import com.ucamp.gyeongjuma_be.common.exception.CustomException;
import com.ucamp.gyeongjuma_be.common.exception.ErrorCode;
import com.ucamp.gyeongjuma_be.favorite.domain.Favorite;
import com.ucamp.gyeongjuma_be.favorite.dto.FavoriteResponse;
import com.ucamp.gyeongjuma_be.favorite.repository.FavoriteRepository;
import com.ucamp.gyeongjuma_be.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final PlaceRepository placeRepository;

    @Override
    @Transactional
    public FavoriteResponse toggleFavorite(Long memberId, Long placeId) {
        if (placeRepository.findById(placeId) == null) {
            throw new CustomException(ErrorCode.PLACE_NOT_FOUND);
        }

        Long favoriteId = favoriteRepository.findIdByMemberIdAndPlaceId(memberId, placeId);
        if (favoriteId != null) {
            favoriteRepository.deleteByMemberIdAndPlaceId(memberId, placeId);
            return new FavoriteResponse(null, placeId, false);
        }

        Favorite favorite = new Favorite(memberId, placeId);
        favoriteRepository.save(favorite);
        return new FavoriteResponse(favorite.getFavoriteId(), placeId, true);
    }
}
