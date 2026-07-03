package com.ucamp.gyeongjuma_be.congestion.controller;

import com.ucamp.gyeongjuma_be.common.dto.ApiResponse;
import com.ucamp.gyeongjuma_be.congestion.dto.CongestionSyncResponse;
import com.ucamp.gyeongjuma_be.congestion.service.CongestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/congestion")
@RequiredArgsConstructor
public class CongestionController {

    private final CongestionService congestionService;

    @PostMapping
    public ResponseEntity<ApiResponse<List<CongestionSyncResponse>>> syncCongestions() {
        List<CongestionSyncResponse> responses = congestionService.syncCongestions();

        ApiResponse<List<CongestionSyncResponse>> apiResponse =
                ApiResponse.success("혼잡도 정보 조회 및 저장이 완료되었습니다.", responses);

        return ResponseEntity.ok(apiResponse);
    }
}
