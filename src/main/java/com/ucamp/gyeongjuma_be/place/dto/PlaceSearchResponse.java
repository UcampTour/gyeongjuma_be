package com.ucamp.gyeongjuma_be.place.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceSearchResponse {

    private Long id;
    private String name;
    private String description;
    private String category;
    private Double rating;
    private Long reviewCount;
    private Long likes;
    private Double lat;
    private Double lng;
    private String congestion;
    private String operationStatus;

    @JsonProperty("isVisited")
    private Boolean isVisited;

    private String imageUrl;
}
