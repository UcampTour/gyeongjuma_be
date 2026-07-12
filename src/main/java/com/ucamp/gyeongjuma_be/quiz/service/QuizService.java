package com.ucamp.gyeongjuma_be.quiz.service;

import com.ucamp.gyeongjuma_be.quiz.dto.response.QuizDetailResponse;
import com.ucamp.gyeongjuma_be.quiz.dto.response.QuizListResponse;
import com.ucamp.gyeongjuma_be.quiz.dto.request.QuizSubmitRequest;
import com.ucamp.gyeongjuma_be.quiz.dto.response.QuizSubmitResponse;
import com.ucamp.gyeongjuma_be.quiz.dto.response.QuizResultResponse;

public interface QuizService {
    QuizListResponse getQuizList(Long memberId);

    QuizDetailResponse getQuizDetail(Long quizId, Long memberId);

    QuizDetailResponse retryQuiz(Long quizId, Long memberId);

    QuizSubmitResponse submitQuiz(Long quizId, Long memberId, QuizSubmitRequest request);

    QuizResultResponse getQuizResult(Long quizId, Long memberId);
}
