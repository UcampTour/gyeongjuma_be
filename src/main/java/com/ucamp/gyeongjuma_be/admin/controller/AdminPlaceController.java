package com.ucamp.gyeongjuma_be.admin.controller;

import com.ucamp.gyeongjuma_be.admin.dto.request.PlaceCreateRequest;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminPlaceDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminPlaceListResponse;
import com.ucamp.gyeongjuma_be.admin.service.AdminPlaceService;
import com.ucamp.gyeongjuma_be.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/places")
@RequiredArgsConstructor
public class AdminPlaceController {

    private final AdminPlaceService adminPlaceService;

    /**
     * 1. 관광지 목록 조회 (검색·페이징, 방문수·퀴즈 수 포함)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<AdminPlaceListResponse>> getPlaces(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "isActive", required = false) Boolean isActive,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        AdminPlaceListResponse response = adminPlaceService.getPlaces(keyword, isActive, page, size);
        return ResponseEntity.ok(ApiResponse.success("관광지 목록 조회에 성공했습니다.", response));
    }

    /**
     * 2. 관광지 추가
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AdminPlaceDto>> createPlace(
            @Valid @RequestBody PlaceCreateRequest request) {
        AdminPlaceDto response = adminPlaceService.createPlace(request);
        return ResponseEntity.ok(ApiResponse.success("관광지를 추가했습니다.", response));
    }

    /**
     * 3. 관광지 삭제 (소프트 삭제 — 방문 이력이 남아 있어 물리 삭제 불가)
     */
    @DeleteMapping("/{placeId}")
    public ResponseEntity<ApiResponse<Void>> deletePlace(@PathVariable Long placeId) {
        adminPlaceService.deletePlace(placeId);
        return ResponseEntity.ok(ApiResponse.success("관광지를 삭제했습니다."));
    }

    /**
     * 4. 삭제한 관광지 복구
     */
    @PatchMapping("/{placeId}/restore")
    public ResponseEntity<ApiResponse<AdminPlaceDto>> restorePlace(@PathVariable Long placeId) {
        AdminPlaceDto response = adminPlaceService.restorePlace(placeId);
        return ResponseEntity.ok(ApiResponse.success("관광지를 복구했습니다.", response));
    }
}
