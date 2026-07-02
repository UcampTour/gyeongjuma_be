package com.ucamp.gyeongjuma_be.place.controller;

import com.ucamp.gyeongjuma_be.common.dto.ApiResponse;
import com.ucamp.gyeongjuma_be.place.dto.PlaceListResponse;
import com.ucamp.gyeongjuma_be.place.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/place")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @PostMapping
    public ResponseEntity<ApiResponse<List<PlaceListResponse>>> syncPlaces() {

        List<PlaceListResponse> responses = placeService.syncPlaces();

        ApiResponse<List<PlaceListResponse>> apiResponse =
                ApiResponse.success("장소 정보 조회 및 저장이 완료되었습니다.", responses);

        return ResponseEntity.ok(apiResponse);
    }
}
