package com.ucamp.gyeongjuma_be.place.controller;

import com.ucamp.gyeongjuma_be.common.dto.ApiResponse;
import com.ucamp.gyeongjuma_be.place.dto.PlaceListResponse;
import com.ucamp.gyeongjuma_be.place.dto.PlaceSearchResponse;
import com.ucamp.gyeongjuma_be.place.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public ResponseEntity<ApiResponse<List<PlaceSearchResponse>>> searchPlaces(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "distance") String sort,
            @RequestParam Double latitude,
            @RequestParam Double longitude
    ) {
        //임시로 memberId = 1로 사용
        Long memberId = 1L;
        List<PlaceSearchResponse> responses = placeService.searchPlaces(memberId, search, sort, latitude, longitude);

        ApiResponse<List<PlaceSearchResponse>> apiResponse =
                ApiResponse.success("관광지 목록 조회를 성공했습니다.", responses);

        return ResponseEntity.ok(apiResponse);
    }
}
