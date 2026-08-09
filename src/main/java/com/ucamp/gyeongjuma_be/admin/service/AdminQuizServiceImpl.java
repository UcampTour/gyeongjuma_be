package com.ucamp.gyeongjuma_be.admin.service;

import com.ucamp.gyeongjuma_be.admin.dto.request.QuizCreateRequest;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminQuizAnswerDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminQuizDetailResponse;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminQuizListResponse;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminQuizQuestionDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminQuizSetDto;
import com.ucamp.gyeongjuma_be.admin.repository.AdminQuizRepository;
import com.ucamp.gyeongjuma_be.common.exception.CustomException;
import com.ucamp.gyeongjuma_be.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminQuizServiceImpl implements AdminQuizService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final AdminQuizRepository adminQuizRepository;

    @Override
    public AdminQuizListResponse getQuizSets(String keyword, Long placeId, String difficulty,
                                             Boolean isActive, int page, int size) {
        int offset = page * size;
        List<AdminQuizSetDto> quizSets =
                adminQuizRepository.findQuizSets(keyword, placeId, difficulty, isActive, offset, size);
        long totalCnt = adminQuizRepository.countQuizSets(keyword, placeId, difficulty, isActive);

        return AdminQuizListResponse.builder()
                .totalCnt(totalCnt)
                .page(page)
                .size(size)
                .totalPages((int) Math.ceil((double) totalCnt / size))
                .quizSets(quizSets)
                .build();
    }

    @Override
    public AdminQuizDetailResponse getQuizDetail(Long placeQuizInfoId) {
        AdminQuizSetDto quizSet = getExistingQuizSet(placeQuizInfoId);
        List<AdminQuizQuestionDto> questions = loadQuestionsWithAnswers(placeQuizInfoId);

        return AdminQuizDetailResponse.builder()
                .quizSet(quizSet)
                .questionCnt(questions.size())
                .questions(questions)
                .build();
    }

    @Override
    @Transactional
    public AdminQuizDetailResponse createQuiz(QuizCreateRequest request) {
        if (!adminQuizRepository.existsActivePlace(request.placeId())) {
            throw new CustomException(ErrorCode.PLACE_NOT_FOUND);
        }

        // 문항마다 정답이 정확히 1개여야 한다
        boolean allValid = request.questions().stream()
                .allMatch(QuizCreateRequest.QuestionRequest::hasExactlyOneCorrectAnswer);
        if (!allValid) {
            throw new CustomException(ErrorCode.INVALID_QUIZ_STRUCTURE);
        }

        LocalDateTime now = LocalDateTime.now(KST);

        adminQuizRepository.insertQuizSet(request.placeId(), request.title(),
                request.description(), request.difficultyOrDefault(), now);
        Long placeQuizInfoId = adminQuizRepository.findLastInsertId();

        for (QuizCreateRequest.QuestionRequest question : request.questions()) {
            adminQuizRepository.insertQuestion(question.question(), now);
            Long quizId = adminQuizRepository.findLastInsertId();

            adminQuizRepository.linkQuizToSet(placeQuizInfoId, quizId);

            for (QuizCreateRequest.AnswerRequest answer : question.answers()) {
                adminQuizRepository.insertAnswer(quizId, answer.content(), answer.isCorrectOrFalse());
            }
        }

        return getQuizDetail(placeQuizInfoId);
    }

    @Override
    @Transactional
    public void deleteQuizSet(Long placeQuizInfoId) {
        AdminQuizSetDto quizSet = getExistingQuizSet(placeQuizInfoId);
        if (Boolean.FALSE.equals(quizSet.getIsActive())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        LocalDateTime now = LocalDateTime.now(KST);
        // 문제집과 그에 속한 문항을 함께 비활성화한다 (응답 이력은 보존)
        adminQuizRepository.softDeleteQuestionsBySetId(placeQuizInfoId, now);
        adminQuizRepository.softDeleteQuizSet(placeQuizInfoId, now);
    }

    private List<AdminQuizQuestionDto> loadQuestionsWithAnswers(Long placeQuizInfoId) {
        List<AdminQuizQuestionDto> questions = adminQuizRepository.findQuestionsBySetId(placeQuizInfoId);
        if (questions.isEmpty()) {
            return questions;
        }

        List<Long> quizIds = questions.stream().map(AdminQuizQuestionDto::getQuizId).toList();
        Map<Long, List<AdminQuizAnswerDto>> answersByQuiz = adminQuizRepository.findAnswersByQuizIds(quizIds)
                .stream()
                .collect(Collectors.groupingBy(AdminQuizAnswerDto::getQuizId));

        questions.forEach(q -> q.setAnswers(answersByQuiz.getOrDefault(q.getQuizId(), List.of())));
        return questions;
    }

    private AdminQuizSetDto getExistingQuizSet(Long placeQuizInfoId) {
        AdminQuizSetDto quizSet = adminQuizRepository.findQuizSetById(placeQuizInfoId);
        if (quizSet == null) {
            throw new CustomException(ErrorCode.QUIZ_NOT_FOUND);
        }
        return quizSet;
    }
}
