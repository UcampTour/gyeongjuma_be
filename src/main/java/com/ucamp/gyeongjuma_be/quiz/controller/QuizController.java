package com.ucamp.gyeongjuma_be.quiz.controller;

import com.ucamp.gyeongjuma_be.common.dto.ApiResponse;
import com.ucamp.gyeongjuma_be.quiz.dto.response.QuizListResponse;
import com.ucamp.gyeongjuma_be.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping
    public ResponseEntity<ApiResponse<QuizListResponse>> getQuizList() {
        //임시로 memberId = 1로 사용
        Long memberId = 1L;
        QuizListResponse response = quizService.getQuizList(memberId);
        ApiResponse<QuizListResponse> apiResponse = ApiResponse.success("퀴즈 목록 조회를 성공했습니다!", response);
        return ResponseEntity.ok(apiResponse);
    }
}
