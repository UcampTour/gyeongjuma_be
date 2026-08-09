package com.ucamp.gyeongjuma_be.admin.service;

import com.ucamp.gyeongjuma_be.admin.dto.request.PlaceCreateRequest;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminPlaceDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminPlaceListResponse;

public interface AdminPlaceService {

    AdminPlaceListResponse getPlaces(String keyword, Boolean isActive, int page, int size);

    AdminPlaceDto createPlace(PlaceCreateRequest request);

    void deletePlace(Long placeId);

    AdminPlaceDto restorePlace(Long placeId);
}
