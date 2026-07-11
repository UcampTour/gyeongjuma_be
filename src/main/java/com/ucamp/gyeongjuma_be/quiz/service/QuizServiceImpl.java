package com.ucamp.gyeongjuma_be.quiz.service;

import com.ucamp.gyeongjuma_be.common.exception.CustomException;
import com.ucamp.gyeongjuma_be.common.exception.ErrorCode;
import com.ucamp.gyeongjuma_be.quiz.dto.request.QuizSubmitRequest;
import com.ucamp.gyeongjuma_be.quiz.dto.response.QuizDetailResponse;
import com.ucamp.gyeongjuma_be.quiz.dto.response.QuizListItem;
import com.ucamp.gyeongjuma_be.quiz.dto.response.QuizListResponse;
import com.ucamp.gyeongjuma_be.quiz.dto.response.QuizSubmitResponse;
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

    @Override
    @Transactional
    public QuizSubmitResponse submitQuiz(Long quizId, Long memberId, QuizSubmitRequest request) {
        QuizSubmitResponse response = quizRepository.findSubmitResult(
                quizId, request.getQuestionId(), request.getSelectedOptionId(), memberId);

        if (response == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        quizRepository.deleteMemberQuizResponse(memberId, request.getQuestionId());
        quizRepository.insertMemberQuizResponse(
                memberId,
                request.getQuestionId(),
                request.getSelectedOptionId(),
                response.getIsCorrect());

        return response;
    }
}
