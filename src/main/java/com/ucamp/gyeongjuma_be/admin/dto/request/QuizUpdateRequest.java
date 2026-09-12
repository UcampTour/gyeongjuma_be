package com.ucamp.gyeongjuma_be.admin.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 퀴즈 세트 수정.
 * 문항·보기는 새로 만들지 않고 기존 행의 내용만 바꾼다 —
 * member_quiz_response가 quiz_id·answer_id를 참조하고 번역본이 origin_quiz_id로 원본을 가리키기 때문에
 * 행을 지웠다 다시 만들면 회원의 퀴즈 기록과 번역본 연결이 끊긴다.
 */
public record QuizUpdateRequest(
        @NotBlank(message = "문제집 제목은 필수입니다.")
        @Size(max = 255, message = "제목은 255자 이내여야 합니다.")
        String title,

        @Size(max = 500, message = "설명은 500자 이내여야 합니다.")
        String description,

        @Pattern(regexp = "^$|^(?i)(EASY|NORMAL|HARD)$", message = "난이도는 EASY, NORMAL, HARD 중 하나여야 합니다.")
        String difficulty,

        /** 사용 여부. 생략하면 현재 값을 유지한다 */
        Boolean isActive,

        @NotEmpty(message = "문항은 1개 이상이어야 합니다.")
        @Valid
        List<QuestionUpdate> questions
) {
    public String difficultyOrNull() {
        return (difficulty == null || difficulty.isBlank()) ? null : difficulty.toUpperCase();
    }

    public record QuestionUpdate(
            @NotNull(message = "문항 ID는 필수입니다.")
            Long quizId,

            @NotBlank(message = "문항 내용은 필수입니다.")
            @Size(max = 500, message = "문항은 500자 이내여야 합니다.")
            String question,

            @NotEmpty(message = "선택지는 2개 이상이어야 합니다.")
            @Size(min = 2, message = "선택지는 2개 이상이어야 합니다.")
            @Valid
            List<AnswerUpdate> answers
    ) {
        public boolean hasExactlyOneCorrectAnswer() {
            return answers.stream().filter(AnswerUpdate::isCorrectOrFalse).count() == 1;
        }
    }

    public record AnswerUpdate(
            @NotNull(message = "선택지 ID는 필수입니다.")
            Long answerId,

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
