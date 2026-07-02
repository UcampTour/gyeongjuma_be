package com.ucamp.gyeongjuma_be.place.service;

import com.ucamp.gyeongjuma_be.place.dto.PlaceListResponse;

import java.util.List;

public interface PlaceService {
    List<PlaceListResponse> syncPlaces();
}
