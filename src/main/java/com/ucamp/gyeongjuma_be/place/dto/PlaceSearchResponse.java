package com.ucamp.gyeongjuma_be.place.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceSearchResponse {

    private Long placeId;
    private String placeName;
    private PlaceDescription description;
    private String category;
    private Long visitCnt; // 방문자수 추가
    private Double lat;
    private Double lng;
    private Double distance;
    private String congestion;
    private String operationStatus;
    private String operationHour;
    private String add1;
    private String add2;
    private String parking;

    @JsonProperty("isVisited")
    private Boolean isVisited;

    @JsonProperty("isFavorite")
    private Boolean isFavorite;

    private String imageUrl;
}
