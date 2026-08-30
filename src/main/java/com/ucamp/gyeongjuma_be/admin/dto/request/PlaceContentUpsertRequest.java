package com.ucamp.gyeongjuma_be.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 관광지 해설 등록·수정 요청.
 * (place_id, language, difficulty)가 같은 해설이 이미 있으면 내용을 갈아끼운다.
 */
public record PlaceContentUpsertRequest(
        @NotBlank(message = "언어 코드는 필수입니다.")
        @Pattern(regexp = "^[a-zA-Z]{2}(-[a-zA-Z]{2,4})?$",
                message = "언어 코드 형식이 올바르지 않습니다. (예: ko, en, zh-Hant)")
        String language,

        @NotBlank(message = "난이도는 필수입니다.")
        @Pattern(regexp = "^(?i)(EASY|NORMAL|HARD)$",
                message = "난이도는 EASY, NORMAL, HARD 중 하나여야 합니다.")
        String difficulty,

        @NotBlank(message = "해설 내용은 필수입니다.")
        String description
) {
    /** DB 컬럼이 enum이라 대문자로 맞춘다 */
    public String normalizedDifficulty() {
        return difficulty.toUpperCase();
    }

    /**
     * 언어 코드는 소문자(ko, en, ja, zh-hant …)로 저장한다 — 기존 place_content 데이터 표기와 맞춘다.
     * 프론트가 대문자(KO)를 보내도 여기서 소문자로 바꾸고, DB collation이 대소문자를 구분하지 않아
     * 기존 행(zh-Hant 등)과도 정상적으로 매칭된다.
     */
    public String normalizedLanguage() {
        return language.toLowerCase();
    }
}
