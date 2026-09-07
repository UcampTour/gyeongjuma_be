package com.ucamp.gyeongjuma_be.admin.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 기존 퀴즈 세트에 문항 하나를 추가한다.
 * 번역본 세트에 추가할 때는 originQuizId로 원본 문항을 지정해야 한다 —
 * 이 연결이 없으면 같은 문제를 언어만 바꿔 다시 풀 때 포인트가 중복 지급된다.
 */
public record QuizQuestionCreateRequest(
        @NotBlank(message = "문항 내용은 필수입니다.")
        @Size(max = 500, message = "문항은 500자 이내여야 합니다.")
        String question,

        /** 번역본 세트에 추가할 때만 사용한다 (원본 세트면 비워 둔다) */
        Long originQuizId,

        @NotEmpty(message = "선택지는 2개 이상이어야 합니다.")
        @Size(min = 2, message = "선택지는 2개 이상이어야 합니다.")
        @Valid
        List<AnswerCreate> answers
) {
    public boolean hasExactlyOneCorrectAnswer() {
        return answers.stream().filter(AnswerCreate::isCorrectOrFalse).count() == 1;
    }

    public record AnswerCreate(
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
