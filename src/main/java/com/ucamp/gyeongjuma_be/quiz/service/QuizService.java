package com.ucamp.gyeongjuma_be.quiz.service;

import com.ucamp.gyeongjuma_be.quiz.dto.QuizListResponse;
import java.util.List;

public interface QuizService {
    List<QuizListResponse> getQuizList();
}
