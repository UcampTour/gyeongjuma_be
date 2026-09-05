package com.ucamp.gyeongjuma_be.admin.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 코스 등록·수정 요청. 관리 화면 형식(type / isUse / places / contents)을 그대로 받는다.
 * places의 나열 순서가 코스를 도는 순서(sort_order 1..N)가 된다.
 */
public record CourseUpsertRequest(
        // WALK(기본), DRIVE, BIKE, TRANSIT
        @Pattern(regexp = "^$|^(?i)(WALK|DRIVE|BIKE|TRANSIT)$",
                message = "코스 유형은 WALK, DRIVE, BIKE, TRANSIT 중 하나여야 합니다.")
        String type,

        /** 사용 여부. 생략하면 사용(true)으로 둔다 */
        Boolean isUse,

        @NotEmpty(message = "코스에 담을 장소를 1개 이상 선택해야 합니다.")
        List<@NotNull(message = "장소 ID는 비어 있을 수 없습니다.") Long> placeIds,

        @NotEmpty(message = "코스 이름을 언어별로 1개 이상 입력해야 합니다.")
        @Valid
        List<CourseContent> contents
) {
    public String typeOrDefault() {
        return (type == null || type.isBlank()) ? "WALK" : type.toUpperCase();
    }

    public boolean isUseOrDefault() {
        return isUse == null || isUse;
    }

    public record CourseContent(
            @NotBlank(message = "언어 코드는 필수입니다.")
            @Pattern(regexp = "^[a-zA-Z]{2}(-[a-zA-Z]{2,4})?$",
                    message = "언어 코드 형식이 올바르지 않습니다. (예: ko, en, zh-Hant)")
            String language,

            @NotBlank(message = "코스 이름은 필수입니다.")
            @Size(max = 100, message = "코스 이름은 100자 이하여야 합니다.")
            String courseName,

            @Size(max = 500, message = "코스 설명은 500자 이하여야 합니다.")
            String description
    ) {
        /** 언어 코드는 소문자로 저장한다 (place_content 표기와 동일) */
        public String normalizedLanguage() {
            return language.toLowerCase();
        }
    }
}
