package com.ucamp.gyeongjuma_be.quiz.service;

import com.ucamp.gyeongjuma_be.quiz.dto.response.QuizDetailResponse;
import com.ucamp.gyeongjuma_be.quiz.dto.response.QuizListResponse;
import com.ucamp.gyeongjuma_be.quiz.dto.request.QuizSubmitRequest;
import com.ucamp.gyeongjuma_be.quiz.dto.response.QuizSubmitResponse;
import com.ucamp.gyeongjuma_be.quiz.dto.response.QuizResultResponse;

public interface QuizService {
    QuizListResponse getQuizList(Long memberId);

    QuizDetailResponse getQuizDetail(Long placeQuizInfoId, Long memberId);

    QuizDetailResponse retryQuiz(Long placeQuizInfoId, Long memberId);

    QuizSubmitResponse submitQuiz(Long placeQuizInfoId, Long memberId, QuizSubmitRequest request);

    QuizResultResponse getQuizResult(Long placeQuizInfoId, Long memberId);
}
