package com.ucamp.gyeongjuma_be.place.service;

import com.ucamp.gyeongjuma_be.place.dto.PlaceListResponse;
import com.ucamp.gyeongjuma_be.place.dto.PlaceSearchResponse;

import java.util.List;
import java.util.Map;

public interface PlaceService {
    List<PlaceListResponse> syncPlaces();

    int syncPlaceDetails();

    int syncPlaceContents();

    Map<String, Integer> syncPlaceContentsAllLanguages();

    List<PlaceSearchResponse> searchPlaces(Long memberId, String search, String sort, Double latitude, Double longitude);
}
