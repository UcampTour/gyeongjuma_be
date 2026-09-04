package com.ucamp.gyeongjuma_be.place.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceSearchResponse {

    private Long placeId;
    private String originalName;
    private String placeName;
    private String subPlaceName;
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

    public void setOriginalName(String originalName) {
        this.originalName = originalName;

        if (originalName == null) {
            this.placeName = null;
            this.subPlaceName = null;
            return;
        }

        int openingIndex = findOpeningParenthesis(originalName);
        if (openingIndex < 0) {
            this.placeName = originalName.trim();
            this.subPlaceName = null;
            return;
        }

        int closingIndex = findClosingParenthesis(originalName, openingIndex);
        if (closingIndex < 0) {
            this.placeName = originalName.trim();
            this.subPlaceName = null;
            return;
        }

        this.placeName = originalName.substring(0, openingIndex).trim();
        this.subPlaceName = originalName.substring(openingIndex + 1, closingIndex).trim();
    }

    private int findOpeningParenthesis(String name) {
        int normalParenthesis = name.indexOf('(');
        int fullWidthParenthesis = name.indexOf('（');

        if (normalParenthesis < 0) {
            return fullWidthParenthesis;
        }
        if (fullWidthParenthesis < 0) {
            return normalParenthesis;
        }
        return Math.min(normalParenthesis, fullWidthParenthesis);
    }

    private int findClosingParenthesis(String name, int openingIndex) {
        char openingParenthesis = name.charAt(openingIndex);
        return name.indexOf(openingParenthesis == '(' ? ')' : '）', openingIndex + 1);
    }
}
