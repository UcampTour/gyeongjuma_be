package com.ucamp.gyeongjuma_be.visit.controller;

import com.ucamp.gyeongjuma_be.auth.AuthInterceptor;
import com.ucamp.gyeongjuma_be.common.dto.ApiResponse;
import com.ucamp.gyeongjuma_be.visit.dto.VisitRequest;
import com.ucamp.gyeongjuma_be.visit.dto.VisitResponse;
import com.ucamp.gyeongjuma_be.visit.service.VisitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/visit")
@RequiredArgsConstructor
public class VisitController {
    private final VisitService visitService;

    @PostMapping("/{placeId}")
    public ResponseEntity<ApiResponse<VisitResponse>> certifyVisit(
            @PathVariable Long placeId,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestAttribute(AuthInterceptor.MEMBER_ID_ATTRIBUTE) Long memberId
    ) {
        VisitResponse response = visitService.certifyVisit(memberId, placeId, new VisitRequest(latitude, longitude));
        return ResponseEntity.ok(ApiResponse.success("방문 인증에 성공했습니다.", response));
    }
}
