package com.ucamp.gyeongjuma_be.place.dto;

import com.ucamp.gyeongjuma_be.place.domain.Place;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlaceListResponse {

    private Long placeId;
    private String placeName;
    private String add1;
    private String add2;
    private String tel;
    private Long contentTypeId;
    private Double mapX;
    private Double mapY;
    private String firstImage;
    private String lclsSystm1;
    private String lclsSystm2;
    private String lclsSystm3;
    private String radiusMeters;

    public static PlaceListResponse from(Place place) {
        return PlaceListResponse.builder()
                .placeId(place.getPlaceId())
                .placeName(place.getPlaceName())
                .add1(place.getAdd1())
                .add2(place.getAdd2())
                .tel(place.getTel())
                .contentTypeId(place.getContentTypeId())
                .mapX(place.getMapX())
                .mapY(place.getMapY())
                .firstImage(place.getFirstImage())
                .lclsSystm1(place.getLclsSystm1())
                .lclsSystm2(place.getLclsSystm2())
                .lclsSystm3(place.getLclsSystm3())
                .radiusMeters(place.getRadiusMeters())
                .add1(place.getAdd1())
                .add2(place.getAdd2())
                .build();
    }
}
