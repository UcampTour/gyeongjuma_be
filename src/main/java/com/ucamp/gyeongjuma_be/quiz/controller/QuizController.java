package com.ucamp.gyeongjuma_be.quiz.controller;

import com.ucamp.gyeongjuma_be.common.dto.ApiResponse;
import com.ucamp.gyeongjuma_be.quiz.dto.QuizListResponse;
import com.ucamp.gyeongjuma_be.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<QuizListResponse>>> getQuizList() {
        List<QuizListResponse> responses = quizService.getQuizList();
        ApiResponse<List<QuizListResponse>> apiResponse =
                ApiResponse.success("퀴즈 목록 조회가 완료되었습니다.", responses);
        return ResponseEntity.ok(apiResponse);
    }
}