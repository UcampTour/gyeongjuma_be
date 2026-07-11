package com.ucamp.gyeongjuma_be.quiz.controller;

import com.ucamp.gyeongjuma_be.common.dto.ApiResponse;
import com.ucamp.gyeongjuma_be.quiz.dto.response.QuizDetailResponse;
import com.ucamp.gyeongjuma_be.quiz.dto.response.QuizListResponse;
import com.ucamp.gyeongjuma_be.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping
    public ResponseEntity<ApiResponse<QuizListResponse>> getQuizList() {
        Long memberId = 1L;
        QuizListResponse response = quizService.getQuizList(memberId);
        ApiResponse<QuizListResponse> apiResponse = ApiResponse.success("퀴즈 목록 조회를 성공했습니다!", response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizDetailResponse>> getQuizDetail(@PathVariable Long quizId) {
        Long memberId = 1L;
        QuizDetailResponse response = quizService.getQuizDetail(quizId, memberId);
        ApiResponse<QuizDetailResponse> apiResponse = ApiResponse.success("퀴즈 상세 정보 조회를 성공했습니다!", response);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{quizId}/retry")
    public ResponseEntity<ApiResponse<QuizDetailResponse>> retryQuiz(@PathVariable Long quizId) {
        Long memberId = 1L;
        QuizDetailResponse response = quizService.retryQuiz(quizId, memberId);
        ApiResponse<QuizDetailResponse> apiResponse = ApiResponse.success("퀴즈를 새로 풀 수 있도록 초기화했습니다!", response);
        return ResponseEntity.ok(apiResponse);
    }
}
