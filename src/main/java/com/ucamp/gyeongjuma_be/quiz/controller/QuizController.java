package com.ucamp.gyeongjuma_be.quiz.controller;

import com.ucamp.gyeongjuma_be.common.dto.ApiResponse;
import com.ucamp.gyeongjuma_be.auth.AuthInterceptor;
import com.ucamp.gyeongjuma_be.quiz.dto.response.QuizDetailResponse;
import com.ucamp.gyeongjuma_be.quiz.dto.response.QuizListResponse;
import com.ucamp.gyeongjuma_be.quiz.dto.request.QuizSubmitRequest;
import com.ucamp.gyeongjuma_be.quiz.dto.response.QuizSubmitResponse;
import com.ucamp.gyeongjuma_be.quiz.dto.response.QuizResultResponse;
import com.ucamp.gyeongjuma_be.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping
    public ResponseEntity<ApiResponse<QuizListResponse>> getQuizList(
            @RequestAttribute(AuthInterceptor.MEMBER_ID_ATTRIBUTE) Long memberId) {
        QuizListResponse response = quizService.getQuizList(memberId);
        ApiResponse<QuizListResponse> apiResponse = ApiResponse.success("퀴즈 목록 조회 성공", response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{placeQuizInfoId}")
    public ResponseEntity<ApiResponse<QuizDetailResponse>> getQuizDetail(
            @PathVariable Long placeQuizInfoId,
            @RequestAttribute(AuthInterceptor.MEMBER_ID_ATTRIBUTE) Long memberId) {
        QuizDetailResponse response = quizService.getQuizDetail(placeQuizInfoId, memberId);
        ApiResponse<QuizDetailResponse> apiResponse = ApiResponse.success("퀴즈 상세 정보 조회 성공", response);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{placeQuizInfoId}/retry")
    public ResponseEntity<ApiResponse<QuizDetailResponse>> retryQuiz(
            @PathVariable Long placeQuizInfoId,
            @RequestAttribute(AuthInterceptor.MEMBER_ID_ATTRIBUTE) Long memberId) {
        QuizDetailResponse response = quizService.retryQuiz(placeQuizInfoId, memberId);
        ApiResponse<QuizDetailResponse> apiResponse = ApiResponse.success("퀴즈 초기화 성공", response);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{placeQuizInfoId}/submit")
    public ResponseEntity<ApiResponse<QuizSubmitResponse>> submitQuiz(
            @PathVariable Long placeQuizInfoId,
            @RequestBody QuizSubmitRequest request,
            @RequestAttribute(AuthInterceptor.MEMBER_ID_ATTRIBUTE) Long memberId
    ) {
        QuizSubmitResponse response = quizService.submitQuiz(placeQuizInfoId, memberId, request);
        ApiResponse<QuizSubmitResponse> apiResponse = ApiResponse.success("퀴즈 답안 제출 성공", response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{placeQuizInfoId}/result")
    public ResponseEntity<ApiResponse<QuizResultResponse>> getQuizResult(
            @PathVariable Long placeQuizInfoId,
            @RequestAttribute(AuthInterceptor.MEMBER_ID_ATTRIBUTE) Long memberId) {
        QuizResultResponse response = quizService.getQuizResult(placeQuizInfoId, memberId);
        ApiResponse<QuizResultResponse> apiResponse = ApiResponse.success("퀴즈 결과 조회 성공", response);
        return ResponseEntity.ok(apiResponse);
    }
}
