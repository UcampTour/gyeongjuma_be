package com.ucamp.gyeongjuma_be.favorite.repository;

import com.ucamp.gyeongjuma_be.favorite.domain.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FavoriteRepository {
    void save(Favorite favorite);

    Long findIdByMemberIdAndPlaceId(@Param("memberId") Long memberId, @Param("placeId") Long placeId);

    void deleteByMemberIdAndPlaceId(@Param("memberId") Long memberId, @Param("placeId") Long placeId);
}
