package com.ucamp.gyeongjuma_be.quiz.service;

import com.ucamp.gyeongjuma_be.quiz.dto.response.QuizDetailResponse;
import com.ucamp.gyeongjuma_be.quiz.dto.response.QuizListItem;
import com.ucamp.gyeongjuma_be.quiz.dto.response.QuizListResponse;
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

        List<QuizListItem> quizList = quizRepository.findQuizList(memberId);
        return QuizListResponse.builder()
                .quizList(quizList)
                .build();
    }

    @Override
    public QuizDetailResponse getQuizDetail(Long quizId, Long memberId) {
        return quizRepository.findQuizDetailByQuizId(quizId, memberId);
    }

    @Override
    @Transactional
    public QuizDetailResponse retryQuiz(Long quizId, Long memberId) {
        quizRepository.deleteQuizResponsesByQuizId(quizId, memberId);
        return quizRepository.findQuizDetailByQuizId(quizId, memberId);
    }
}
