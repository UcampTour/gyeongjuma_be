package com.ucamp.gyeongjuma_be.place.domain;

import com.ucamp.gyeongjuma_be.common.domain.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Place extends BaseEntity {

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

    @Builder
    public Place(Long placeId, String placeName, String add1, String add2, String tel,
                 Long contentTypeId, Double mapX, Double mapY, String firstImage,
                 String lclsSystm1, String lclsSystm2, String lclsSystm3, String radiusMeters) {
        this.placeId = placeId;
        this.placeName = placeName;
        this.add1 = add1;
        this.add2 = add2== null ? "" : add2;
        this.tel = tel== null ? "" : tel;
        this.contentTypeId = contentTypeId;
        this.mapX = mapX;
        this.mapY = mapY;
        this.firstImage = firstImage == null ? "" : firstImage;
        this.lclsSystm1 = lclsSystm1;
        this.lclsSystm2 = lclsSystm2;
        this.lclsSystm3 = lclsSystm3;
        this.radiusMeters = radiusMeters == null ? "0" : radiusMeters;
    }
}
