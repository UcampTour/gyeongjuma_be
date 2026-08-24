package com.ucamp.gyeongjuma_be.admin.service;

import com.ucamp.gyeongjuma_be.admin.dto.request.QuizCreateRequest;
import com.ucamp.gyeongjuma_be.admin.dto.request.QuizTranslationRequest;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminQuizDetailResponse;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminQuizListResponse;

public interface AdminQuizService {

    AdminQuizListResponse getQuizSets(String keyword, Long placeId, String difficulty,
                                      String language, Boolean isActive, int page, int size);

    AdminQuizDetailResponse getQuizDetail(Long placeQuizInfoId);

    AdminQuizDetailResponse createQuiz(QuizCreateRequest request);

    AdminQuizDetailResponse createTranslation(Long placeQuizInfoId, QuizTranslationRequest request);

    java.util.List<com.ucamp.gyeongjuma_be.admin.dto.response.AdminQuizSetDto> getTranslations(Long placeQuizInfoId);

    void deleteQuizSet(Long placeQuizInfoId);
}
