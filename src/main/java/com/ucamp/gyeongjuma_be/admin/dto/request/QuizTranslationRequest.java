package com.ucamp.gyeongjuma_be.admin.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 기존 퀴즈 세트의 번역본 등록 요청.
 * 원본 세트는 경로 변수로 받으므로 여기에는 담지 않는다.
 * 문항마다 originQuizId로 어떤 원본 문항의 번역인지 지정한다 — 이 연결이 포인트 중복 지급을 막는다.
 */
public record QuizTranslationRequest(
        @NotBlank(message = "언어 코드는 필수입니다.")
        @Pattern(regexp = "^[a-zA-Z]{2}(-[a-zA-Z]{2,4})?$",
                message = "언어 코드 형식이 올바르지 않습니다. (예: en, ja, zh-Hant)")
        String language,

        @NotBlank(message = "문제집 제목은 필수입니다.")
        @Size(max = 255, message = "제목은 255자 이내여야 합니다.")
        String title,

        @Size(max = 500, message = "설명은 500자 이내여야 합니다.")
        String description,

        @NotEmpty(message = "문항은 1개 이상이어야 합니다.")
        @Valid
        List<TranslatedQuestion> questions
) {
    /** 언어 코드는 소문자로 통일해 저장한다 (DB collation이 대소문자를 구분하지 않지만 값 자체를 일관되게 둔다) */
    public String normalizedLanguage() {
        return language.toLowerCase();
    }

    public record TranslatedQuestion(
            @NotNull(message = "원본 문항 ID는 필수입니다.")
            Long originQuizId,

            @NotBlank(message = "문항 내용은 필수입니다.")
            @Size(max = 500, message = "문항은 500자 이내여야 합니다.")
            String question,

            @NotEmpty(message = "선택지는 2개 이상이어야 합니다.")
            @Size(min = 2, message = "선택지는 2개 이상이어야 합니다.")
            @Valid
            List<TranslatedAnswer> answers
    ) {
        public boolean hasExactlyOneCorrectAnswer() {
            return answers.stream().filter(TranslatedAnswer::isCorrectOrFalse).count() == 1;
        }
    }

    public record TranslatedAnswer(
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
