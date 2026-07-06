package com.ucamp.gyeongjuma_be.place.service;

import com.ucamp.gyeongjuma_be.place.dto.PlaceListResponse;
import com.ucamp.gyeongjuma_be.place.dto.PlaceSearchResponse;

import java.util.List;

public interface PlaceService {
    List<PlaceListResponse> syncPlaces();

    List<PlaceSearchResponse> searchPlaces(Long memberId, String search, String sort, Double latitude, Double longitude);
}
