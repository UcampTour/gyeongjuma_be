package com.ucamp.gyeongjuma_be.admin.controller;

import com.ucamp.gyeongjuma_be.admin.dto.request.QuizCreateRequest;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminQuizDetailResponse;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminQuizListResponse;
import com.ucamp.gyeongjuma_be.admin.service.AdminQuizService;
import com.ucamp.gyeongjuma_be.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/quizzes")
@RequiredArgsConstructor
public class AdminQuizController {

    private final AdminQuizService adminQuizService;

    /**
     * 1. 퀴즈 문제집 목록 조회 (관광지명·난이도 필터, 페이징)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<AdminQuizListResponse>> getQuizSets(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "placeId", required = false) Long placeId,
            @RequestParam(value = "difficulty", required = false) String difficulty,
            @RequestParam(value = "isActive", required = false) Boolean isActive,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        AdminQuizListResponse response =
                adminQuizService.getQuizSets(keyword, placeId, difficulty, isActive, page, size);
        return ResponseEntity.ok(ApiResponse.success("퀴즈 목록 조회에 성공했습니다.", response));
    }

    /**
     * 2. 퀴즈 문제집 상세 조회 (문항 + 선택지 전체)
     */
    @GetMapping("/{placeQuizInfoId}")
    public ResponseEntity<ApiResponse<AdminQuizDetailResponse>> getQuizDetail(
            @PathVariable Long placeQuizInfoId) {
        AdminQuizDetailResponse response = adminQuizService.getQuizDetail(placeQuizInfoId);
        return ResponseEntity.ok(ApiResponse.success("퀴즈 상세 조회에 성공했습니다.", response));
    }

    /**
     * 3. 퀴즈 등록 (문제집 + 문항 + 선택지를 한 번에)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AdminQuizDetailResponse>> createQuiz(
            @Valid @RequestBody QuizCreateRequest request) {
        AdminQuizDetailResponse response = adminQuizService.createQuiz(request);
        return ResponseEntity.ok(ApiResponse.success("퀴즈를 등록했습니다.", response));
    }

    /**
     * 4. 퀴즈 문제집 삭제 (문제집·문항 비활성화, 응답 이력은 보존)
     */
    @DeleteMapping("/{placeQuizInfoId}")
    public ResponseEntity<ApiResponse<Void>> deleteQuizSet(@PathVariable Long placeQuizInfoId) {
        adminQuizService.deleteQuizSet(placeQuizInfoId);
        return ResponseEntity.ok(ApiResponse.success("퀴즈를 삭제했습니다."));
    }
}
