package com.ucamp.gyeongjuma_be.place.controller;

import com.ucamp.gyeongjuma_be.common.dto.ApiResponse;
import com.ucamp.gyeongjuma_be.place.dto.PlaceSearchResponse;
import com.ucamp.gyeongjuma_be.place.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SearchPlaceController {

    private final PlaceService placeService;

    @GetMapping("/searchPlace")
    public ResponseEntity<ApiResponse<List<PlaceSearchResponse>>> searchPlaces(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "distance") String sort,
            @RequestParam Double latitude,
            @RequestParam Double longitude
    ) {
        List<PlaceSearchResponse> responses = placeService.searchPlaces(search, sort, latitude, longitude);

        ApiResponse<List<PlaceSearchResponse>> apiResponse =
                ApiResponse.success("관광지 목록 조회를 성공했습니다.", responses);

        return ResponseEntity.ok(apiResponse);
    }
}
