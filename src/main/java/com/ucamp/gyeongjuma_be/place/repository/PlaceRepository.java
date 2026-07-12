package com.ucamp.gyeongjuma_be.place.repository;

import com.ucamp.gyeongjuma_be.place.domain.Place;
import com.ucamp.gyeongjuma_be.place.dto.PlaceSearchResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlaceRepository {

    void saveAll(@Param("places") List<Place> places);

    int updateDetail(Place place);

    void deletePlaceContent(@Param("placeId") Long placeId,
                            @Param("language") String language,
                            @Param("difficulty") String difficulty);

    int savePlaceContent(@Param("placeId") Long placeId,
                         @Param("description") String description,
                         @Param("language") String language,
                         @Param("difficulty") String difficulty);

    List<Place> findAll();

    List<PlaceSearchResponse> searchPlaces(@Param("search") String search,
                                            @Param("sort") String sort,
                                            @Param("latitude") Double latitude,
                                            @Param("longitude") Double longitude,
                                            @Param("memberId") Long memberId);
}
