package com.ucamp.gyeongjuma_be.admin.service;

import com.ucamp.gyeongjuma_be.admin.dto.request.PlaceContentUpsertRequest;
import com.ucamp.gyeongjuma_be.admin.dto.request.PlaceCreateRequest;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminPlaceDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminPlaceItemDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminPlaceItemListResponse;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminPlaceListResponse;

public interface AdminPlaceService {

    AdminPlaceListResponse getPlaces(String keyword, Boolean isActive, int page, int size);

    AdminPlaceDto createPlace(PlaceCreateRequest request);

    void deletePlace(Long placeId);

    AdminPlaceDto restorePlace(Long placeId);

    /** 관광지 관리 화면용 목록 — 관광지 + 언어·난이도별 해설 */
    AdminPlaceItemListResponse getPlaceItems(String keyword, Boolean isActive, int page, int size);

    AdminPlaceItemDto getPlaceItem(Long placeId);

    /** 해설 등록·수정 — 같은 (언어, 난이도) 해설이 있으면 내용을 교체한다 */
    AdminPlaceItemDto upsertContent(Long placeId, PlaceContentUpsertRequest request);

    void deleteContent(Long placeId, Long placeContentId);
}
