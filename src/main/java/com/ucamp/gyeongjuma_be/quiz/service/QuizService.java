package com.ucamp.gyeongjuma_be.quiz.service;

import com.ucamp.gyeongjuma_be.quiz.dto.response.QuizDetailResponse;
import com.ucamp.gyeongjuma_be.quiz.dto.response.QuizListResponse;

public interface QuizService {
    QuizListResponse getQuizList(Long memberId);

    QuizDetailResponse getQuizDetail(Long quizId, Long memberId);

    QuizDetailResponse retryQuiz(Long quizId, Long memberId);
}
