package com.ucamp.gyeongjuma_be.admin.service;

import com.ucamp.gyeongjuma_be.admin.dto.request.QuizCreateRequest;
import com.ucamp.gyeongjuma_be.admin.dto.request.QuizQuestionCreateRequest;
import com.ucamp.gyeongjuma_be.admin.dto.request.QuizTranslationRequest;
import com.ucamp.gyeongjuma_be.admin.dto.request.QuizUpdateRequest;
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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminQuizServiceImpl implements AdminQuizService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final AdminQuizRepository adminQuizRepository;

    @Override
    public AdminQuizListResponse getQuizSets(String keyword, Long placeId, String difficulty,
                                             String language, Boolean isActive, int page, int size) {
        int offset = page * size;
        List<AdminQuizSetDto> quizSets =
                adminQuizRepository.findQuizSets(keyword, placeId, difficulty, language, isActive, offset, size);
        long totalCnt = adminQuizRepository.countQuizSets(keyword, placeId, difficulty, language, isActive);

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

        // 신규 등록은 항상 원본이므로 originInfoId는 null
        adminQuizRepository.insertQuizSet(request.placeId(), request.title(),
                request.description(), request.difficultyOrDefault(),
                request.languageOrDefault(), null, now);
        Long placeQuizInfoId = adminQuizRepository.findLastInsertId();

        for (QuizCreateRequest.QuestionRequest question : request.questions()) {
            adminQuizRepository.insertQuestion(question.question(), null, now);
            Long quizId = adminQuizRepository.findLastInsertId();

            adminQuizRepository.linkQuizToSet(placeQuizInfoId, quizId);

            for (QuizCreateRequest.AnswerRequest answer : question.answers()) {
                adminQuizRepository.insertAnswer(quizId, answer.content(), answer.isCorrectOrFalse());
            }
        }

        return getQuizDetail(placeQuizInfoId);
    }

    /**
     * 퀴즈 세트 수정.
     * 문항·보기는 새로 만들지 않고 기존 행의 내용만 바꾼다 —
     * member_quiz_response가 quiz_id·answer_id를 참조하고, 번역본이 origin_quiz_id로 원본을 가리키기 때문에
     * 행을 지웠다 다시 만들면 회원의 퀴즈 기록과 번역본 연결이 함께 끊긴다.
     */
    @Override
    @Transactional
    public AdminQuizDetailResponse updateQuiz(Long placeQuizInfoId, QuizUpdateRequest request) {
        getExistingQuizSet(placeQuizInfoId);

        // 문항마다 정답이 정확히 1개여야 한다 (등록과 동일한 규칙)
        boolean allValid = request.questions().stream()
                .allMatch(QuizUpdateRequest.QuestionUpdate::hasExactlyOneCorrectAnswer);
        if (!allValid) {
            throw new CustomException(ErrorCode.INVALID_QUIZ_STRUCTURE);
        }

        // 이 세트의 문항과 1:1로 대응해야 한다 — 빠뜨림·중복·남의 문항 지정을 모두 막는다
        List<Long> setQuizIds = adminQuizRepository.findQuizIdsBySetId(placeQuizInfoId);
        Set<Long> requested = new LinkedHashSet<>();
        for (QuizUpdateRequest.QuestionUpdate q : request.questions()) {
            if (!requested.add(q.quizId())) {
                throw new CustomException(ErrorCode.INVALID_QUIZ_STRUCTURE);
            }
        }
        if (requested.size() != setQuizIds.size() || !requested.containsAll(setQuizIds)) {
            throw new CustomException(ErrorCode.INVALID_QUIZ_STRUCTURE);
        }

        LocalDateTime now = LocalDateTime.now(KST);
        adminQuizRepository.updateQuizSet(placeQuizInfoId, request.title(), request.description(),
                request.difficultyOrNull(), request.isActive(), now);

        boolean correctAnswerChanged = false;
        for (QuizUpdateRequest.QuestionUpdate question : request.questions()) {
            // 보기도 그 문항의 것과 정확히 일치해야 한다
            List<Long> answerIds = adminQuizRepository.findAnswerIdsByQuizId(question.quizId());
            Set<Long> requestedAnswers = new LinkedHashSet<>();
            for (QuizUpdateRequest.AnswerUpdate a : question.answers()) {
                if (!requestedAnswers.add(a.answerId())) {
                    throw new CustomException(ErrorCode.INVALID_QUIZ_STRUCTURE);
                }
            }
            if (requestedAnswers.size() != answerIds.size() || !requestedAnswers.containsAll(answerIds)) {
                throw new CustomException(ErrorCode.INVALID_QUIZ_STRUCTURE);
            }

            adminQuizRepository.updateQuestion(question.quizId(), question.question(), now);
            for (QuizUpdateRequest.AnswerUpdate answer : question.answers()) {
                adminQuizRepository.updateAnswer(answer.answerId(), answer.content(), answer.isCorrectOrFalse());
            }
            correctAnswerChanged = true;
        }

        // 정답이 바뀌었을 수 있으므로 기존 응답의 정답 여부를 새 기준으로 다시 계산한다
        if (correctAnswerChanged && !setQuizIds.isEmpty()) {
            adminQuizRepository.recalculateResponses(setQuizIds);
        }

        return getQuizDetail(placeQuizInfoId);
    }

    /**
     * 세트에 문항 하나를 추가한다.
     * 번역본 세트에 추가할 때는 originQuizId로 원본 문항을 지정해야 한다 —
     * 그 연결이 없으면 같은 문제를 언어만 바꿔 다시 풀 때 포인트가 중복 지급된다.
     */
    @Override
    @Transactional
    public AdminQuizDetailResponse addQuestion(Long placeQuizInfoId, QuizQuestionCreateRequest request) {
        AdminQuizSetDto set = getExistingQuizSet(placeQuizInfoId);
        if (Boolean.FALSE.equals(set.getIsActive())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (!request.hasExactlyOneCorrectAnswer()) {
            throw new CustomException(ErrorCode.INVALID_QUIZ_STRUCTURE);
        }

        Long originQuizId = resolveOriginQuizId(set, request.originQuizId());

        LocalDateTime now = LocalDateTime.now(KST);
        adminQuizRepository.insertQuestion(request.question(), originQuizId, now);
        Long quizId = adminQuizRepository.findLastInsertId();

        adminQuizRepository.linkQuizToSet(placeQuizInfoId, quizId);
        for (QuizQuestionCreateRequest.AnswerCreate answer : request.answers()) {
            adminQuizRepository.insertAnswer(quizId, answer.content(), answer.isCorrectOrFalse());
        }

        return getQuizDetail(placeQuizInfoId);
    }

    /**
     * 문항 삭제. 항상 비활성화만 한다 —
     * 회원 응답과 번역본이 이 문항을 참조하고 있어 행을 지울 수 없다.
     */
    @Override
    @Transactional
    public AdminQuizDetailResponse deleteQuestion(Long placeQuizInfoId, Long quizId) {
        getExistingQuizSet(placeQuizInfoId);

        List<Long> setQuizIds = adminQuizRepository.findQuizIdsBySetId(placeQuizInfoId);
        if (!setQuizIds.contains(quizId)) {
            throw new CustomException(ErrorCode.QUIZ_NOT_FOUND);
        }
        // 문항이 하나도 없는 세트는 풀 수 없으므로 마지막 문항은 남긴다
        if (setQuizIds.size() <= 1) {
            throw new CustomException(ErrorCode.INVALID_QUIZ_STRUCTURE);
        }

        adminQuizRepository.softDeleteQuestion(quizId, LocalDateTime.now(KST));
        return getQuizDetail(placeQuizInfoId);
    }

    /** 번역본 세트면 원본 문항을 반드시 지정해야 하고, 원본 세트면 지정하면 안 된다 */
    private Long resolveOriginQuizId(AdminQuizSetDto set, Long requestedOriginQuizId) {
        boolean isTranslation = set.getOriginInfoId() != null;

        if (!isTranslation) {
            if (requestedOriginQuizId != null) {
                throw new CustomException(ErrorCode.INVALID_QUIZ_TRANSLATION);
            }
            return null;
        }
        if (requestedOriginQuizId == null) {
            throw new CustomException(ErrorCode.INVALID_QUIZ_TRANSLATION);
        }
        // 원본 세트에 실제로 있는 문항인지, 이미 번역된 문항은 아닌지 확인한다
        List<Long> originQuizIds = adminQuizRepository.findQuizIdsBySetId(set.getOriginInfoId());
        if (!originQuizIds.contains(requestedOriginQuizId)) {
            throw new CustomException(ErrorCode.INVALID_QUIZ_TRANSLATION);
        }
        boolean alreadyTranslated = adminQuizRepository.findQuestionsBySetId(set.getPlaceQuizInfoId()).stream()
                .anyMatch(q -> requestedOriginQuizId.equals(q.getOriginQuizId()));
        if (alreadyTranslated) {
            throw new CustomException(ErrorCode.INVALID_QUIZ_TRANSLATION);
        }
        return requestedOriginQuizId;
    }

    /**
     * 기존 세트의 번역본 등록.
     * 세트와 문항을 언어별로 복제하되, 복제본이 원본을 가리키게 해서(origin_info_id / origin_quiz_id)
     * 같은 문제를 언어만 바꿔 다시 풀 때 포인트가 중복 지급되지 않도록 한다.
     */
    @Override
    @Transactional
    public AdminQuizDetailResponse createTranslation(Long placeQuizInfoId, QuizTranslationRequest request) {
        AdminQuizSetDto origin = getExistingQuizSet(placeQuizInfoId);

        // 번역본의 번역본은 만들지 않는다 — origin 체인이 깊어지면 중복 방지 키가 어긋난다
        if (origin.getOriginInfoId() != null) {
            throw new CustomException(ErrorCode.INVALID_QUIZ_TRANSLATION);
        }
        if (Boolean.FALSE.equals(origin.getIsActive())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String language = request.normalizedLanguage();
        // 원본과 같은 언어로는 번역본을 만들 수 없다
        if (language.equalsIgnoreCase(origin.getLanguage())) {
            throw new CustomException(ErrorCode.INVALID_QUIZ_TRANSLATION);
        }
        if (adminQuizRepository.existsTranslation(placeQuizInfoId, language)) {
            throw new CustomException(ErrorCode.DUPLICATE_QUIZ_TRANSLATION);
        }

        // 문항마다 정답이 정확히 1개여야 한다 (원본 등록과 동일한 규칙)
        boolean allValid = request.questions().stream()
                .allMatch(QuizTranslationRequest.TranslatedQuestion::hasExactlyOneCorrectAnswer);
        if (!allValid) {
            throw new CustomException(ErrorCode.INVALID_QUIZ_STRUCTURE);
        }

        // 원본 문항과 1:1로 대응해야 한다 — 빠뜨림·중복·남의 문항 지정을 모두 막는다
        List<Long> originQuizIds = adminQuizRepository.findQuizIdsBySetId(placeQuizInfoId);
        Set<Long> requested = new LinkedHashSet<>();
        for (QuizTranslationRequest.TranslatedQuestion q : request.questions()) {
            if (!requested.add(q.originQuizId())) {
                throw new CustomException(ErrorCode.INVALID_QUIZ_TRANSLATION);
            }
        }
        if (requested.size() != originQuizIds.size() || !requested.containsAll(originQuizIds)) {
            throw new CustomException(ErrorCode.INVALID_QUIZ_TRANSLATION);
        }

        LocalDateTime now = LocalDateTime.now(KST);

        // 번역본 세트는 원본과 같은 장소·난이도를 쓰고 언어만 달라진다
        adminQuizRepository.insertQuizSet(origin.getPlaceId(), request.title(),
                request.description(), origin.getDifficulty(), language, placeQuizInfoId, now);
        Long translatedSetId = adminQuizRepository.findLastInsertId();

        for (QuizTranslationRequest.TranslatedQuestion question : request.questions()) {
            adminQuizRepository.insertQuestion(question.question(), question.originQuizId(), now);
            Long quizId = adminQuizRepository.findLastInsertId();

            adminQuizRepository.linkQuizToSet(translatedSetId, quizId);

            for (QuizTranslationRequest.TranslatedAnswer answer : question.answers()) {
                adminQuizRepository.insertAnswer(quizId, answer.content(), answer.isCorrectOrFalse());
            }
        }

        return getQuizDetail(translatedSetId);
    }

    @Override
    public List<AdminQuizSetDto> getTranslations(Long placeQuizInfoId) {
        getExistingQuizSet(placeQuizInfoId);
        return adminQuizRepository.findTranslationsByOriginId(placeQuizInfoId);
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
