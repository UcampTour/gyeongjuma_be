package com.ucamp.gyeongjuma_be.audio.controller;

import com.ucamp.gyeongjuma_be.audio.service.AudioService;
import com.ucamp.gyeongjuma_be.audio.dto.AudioResponse;
import com.ucamp.gyeongjuma_be.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/audio")
@RequiredArgsConstructor
public class AudioController {

    private final AudioService audioService;

    @PostMapping
    public ResponseEntity<ApiResponse<Integer>> syncAudios() {
        int savedCount = audioService.syncAudios();
        return ResponseEntity.ok(ApiResponse.success("오디오 정보 저장이 완료되었습니다.", savedCount));
    }

    @GetMapping("/{placeId}")
    public ResponseEntity<ApiResponse<List<AudioResponse>>> getAudios(@PathVariable Long placeId) {
        List<AudioResponse> responses = audioService.getAudios(placeId);
        return ResponseEntity.ok(ApiResponse.success("장소 오디오 정보를 조회했습니다.", responses));
    }
}
