package com.ucamp.gyeongjuma_be.admin.service;

import com.ucamp.gyeongjuma_be.admin.dto.request.QuizCreateRequest;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminQuizDetailResponse;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminQuizListResponse;

public interface AdminQuizService {

    AdminQuizListResponse getQuizSets(String keyword, Long placeId, String difficulty,
                                      Boolean isActive, int page, int size);

    AdminQuizDetailResponse getQuizDetail(Long placeQuizInfoId);

    AdminQuizDetailResponse createQuiz(QuizCreateRequest request);

    void deleteQuizSet(Long placeQuizInfoId);
}
