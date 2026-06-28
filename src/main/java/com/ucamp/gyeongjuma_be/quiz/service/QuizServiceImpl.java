package com.ucamp.gyeongjuma_be.quiz.service;

import com.ucamp.gyeongjuma_be.quiz.dto.QuizListResponse;
import com.ucamp.gyeongjuma_be.quiz.mapper.QuizMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizMapper quizMapper;

    @Override
    public List<QuizListResponse> getQuizList() {
        return quizMapper.findAll().stream()
                .map(QuizListResponse::new)
                .collect(Collectors.toList());
    }
}
