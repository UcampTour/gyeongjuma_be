package com.ucamp.gyeongjuma_be.admin.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 퀴즈 문제집을 문항·선택지까지 한 번에 등록한다.
 * place_quiz_info(문제집) → quiz(문항) → quiz_answer(선택지) 3단 구조를 하나의 트랜잭션으로 저장한다.
 */
public record QuizCreateRequest(
        @NotNull(message = "관광지 ID는 필수입니다.")
        Long placeId,

        @NotBlank(message = "문제집 제목은 필수입니다.")
        @Size(max = 255, message = "제목은 255자 이내여야 합니다.")
        String title,

        @Size(max = 500, message = "설명은 500자 이내여야 합니다.")
        String description,

        @Pattern(regexp = "^$|^(?i)(EASY|NORMAL|HARD)$", message = "난이도는 EASY, NORMAL, HARD 중 하나여야 합니다.")
        String difficulty,

        // ko, en 등 언어 코드 (생략 시 ko)
        @Pattern(regexp = "^$|^[a-zA-Z]{2}(-[a-zA-Z]{2,4})?$", message = "언어 코드 형식이 올바르지 않습니다. (예: ko, en)")
        String language,

        @NotEmpty(message = "문항은 1개 이상이어야 합니다.")
        @Valid
        List<QuestionRequest> questions
) {
    public String difficultyOrDefault() {
        return (difficulty == null || difficulty.isBlank()) ? "NORMAL" : difficulty.toUpperCase();
    }

    public String languageOrDefault() {
        return (language == null || language.isBlank()) ? "ko" : language.toLowerCase();
    }

    public record QuestionRequest(
            @NotBlank(message = "문항 내용은 필수입니다.")
            @Size(max = 500, message = "문항은 500자 이내여야 합니다.")
            String question,

            @NotEmpty(message = "선택지는 2개 이상이어야 합니다.")
            @Size(min = 2, message = "선택지는 2개 이상이어야 합니다.")
            @Valid
            List<AnswerRequest> answers
    ) {
        /** 정답이 정확히 1개인지 확인 */
        public boolean hasExactlyOneCorrectAnswer() {
            return answers.stream().filter(AnswerRequest::isCorrectOrFalse).count() == 1;
        }
    }

    public record AnswerRequest(
            @NotBlank(message = "선택지 내용은 필수입니다.")
            @Size(max = 255, message = "선택지는 255자 이내여야 합니다.")
            String content,

            Boolean isCorrect
    ) {
        public boolean isCorrectOrFalse() {
            return Boolean.TRUE.equals(isCorrect);
        }
    }
}
