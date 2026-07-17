package com.ucamp.gyeongjuma_be.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ExtraInfoRequest(
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 12, message = "닉네임은 2~12자여야 합니다.")
        @Pattern(regexp = "^[가-힣a-zA-Z0-9]+$", message = "닉네임은 한글, 영문, 숫자만 사용할 수 있습니다.")
        String nickname,

        // NORMAL, HARD만 허용 (생략 시 NORMAL)
        @Pattern(regexp = "^$|^(?i)(NORMAL|HARD)$", message = "난이도는 NORMAL 또는 HARD만 선택할 수 있습니다.")
        String difficulty,

        // ko, en 등 언어 코드 (생략 시 ko)
        @Pattern(regexp = "^$|^[a-zA-Z]{2}(-[a-zA-Z]{2,4})?$", message = "언어 코드 형식이 올바르지 않습니다. (예: ko, en)")
        String locale
) {
    public String difficultyOrDefault() {
        return (difficulty == null || difficulty.isBlank()) ? "NORMAL" : difficulty.toUpperCase();
    }

    public String localeOrDefault() {
        return (locale == null || locale.isBlank()) ? "ko" : locale.toLowerCase();
    }
}
