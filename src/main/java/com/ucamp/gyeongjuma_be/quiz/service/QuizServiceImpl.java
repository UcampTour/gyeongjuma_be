package com.ucamp.gyeongjuma_be.quiz.service;

import com.ucamp.gyeongjuma_be.quiz.dto.response.QuizDetailResponse;
import com.ucamp.gyeongjuma_be.quiz.dto.response.QuizListResponse;
import com.ucamp.gyeongjuma_be.quiz.dto.response.lockedPlaceQuizDto;
import com.ucamp.gyeongjuma_be.quiz.dto.response.visitedPlaceQuizDto;
import com.ucamp.gyeongjuma_be.quiz.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;

    @Override
    public QuizListResponse getQuizList(Long memberId) {

        List<visitedPlaceQuizDto> visitedPlaceQuizList = quizRepository.findVisitedPlaceQuizList(memberId);
        List<lockedPlaceQuizDto> lockedPlaceQuizList = quizRepository.findLockedPlaceQuizList(memberId);

        return QuizListResponse.builder()
                .visitedPlaceQuizDtoList(visitedPlaceQuizList)
                .lockedPlaceQuizDtoList(lockedPlaceQuizList)
                .build();
    }

    @Override
    public QuizDetailResponse getQuizDetail(Long placeId, Long memberId) {
        return quizRepository.findQuizDetailByPlaceId(placeId, memberId);
    }

    @Override
    @Transactional
    public QuizDetailResponse retryQuiz(Long placeId, Long memberId) {
        quizRepository.deleteQuizResponsesByPlaceId(placeId, memberId);
        return quizRepository.findQuizDetailByPlaceId(placeId, memberId);
    }
}
