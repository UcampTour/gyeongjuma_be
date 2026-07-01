package com.ucamp.gyeongjuma_be.quiz.service;

import com.ucamp.gyeongjuma_be.quiz.dto.QuizListResponse;
import com.ucamp.gyeongjuma_be.quiz.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;

    @Override
    public List<QuizListResponse> getQuizList() {
        return quizRepository.findAll().stream()
                .map(QuizListResponse::new)
                .collect(Collectors.toList());
    }
}
