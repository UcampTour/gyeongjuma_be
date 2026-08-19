package com.ucamp.gyeongjuma_be.member.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 닉네임·난이도·언어 부분 수정 요청.
 * 세 필드 모두 선택값이며, 보내지 않은(null) 필드는 기존 값을 그대로 유지한다.
 * 최초 등록용 {@link ExtraInfoRequest}와 달리 생략값에 기본값(NORMAL/ko)을 채우지 않는다.
 */
public record MemberUpdateRequest(
        @Size(min = 2, max = 12, message = "닉네임은 2~12자여야 합니다.")
        @Pattern(regexp = "^[가-힣a-zA-Z0-9]+$", message = "닉네임은 한글, 영문, 숫자만 사용할 수 있습니다.")
        String nickname,

        // DB 컬럼이 enum('EASY','NORMAL','HARD')이라 저장 시 대문자로 변환한다
        @Pattern(regexp = "^(?i)(EASY|NORMAL|HARD)$", message = "난이도는 EASY, NORMAL, HARD 중 하나여야 합니다.")
        String difficulty,

        // ko, en 등 언어 코드 (DB 컬럼 varchar(10))
        @Pattern(regexp = "^[a-zA-Z]{2}(-[a-zA-Z]{2,4})?$", message = "언어 코드 형식이 올바르지 않습니다. (예: ko, en)")
        String locale
) {
    /** 수정할 필드가 하나도 없으면 의미 없는 요청이다. */
    public boolean isEmpty() {
        return nickname == null && difficulty == null && locale == null;
    }

    public String normalizedDifficulty() {
        return difficulty == null ? null : difficulty.toUpperCase();
    }

    public String normalizedLocale() {
        return locale == null ? null : locale.toLowerCase();
    }
}
