package com.ucamp.gyeongjuma_be.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ExtraInfoRequest(
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 12, message = "닉네임은 2~12자여야 합니다.")
        @Pattern(regexp = "^[가-힣a-zA-Z0-9]+$", message = "닉네임은 한글, 영문, 숫자만 사용할 수 있습니다.")
        String nickname,

        // EASY, NORMAL, HARD (생략 시 EASY)
        String difficulty
) {
    public String difficultyOrDefault() {
        return (difficulty == null || difficulty.isBlank()) ? "EASY" : difficulty.toUpperCase();
    }
}
