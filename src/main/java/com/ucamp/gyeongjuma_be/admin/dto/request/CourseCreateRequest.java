package com.ucamp.gyeongjuma_be.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 코스 등록 요청. placeIds의 나열 순서가 곧 코스를 도는 순서(sort_order 1..N)가 된다.
 */
public record CourseCreateRequest(
        @NotBlank(message = "코스 이름은 필수입니다.")
        @Size(max = 100, message = "코스 이름은 100자 이하여야 합니다.")
        String name,

        @Size(max = 500, message = "코스 설명은 500자 이하여야 합니다.")
        String description,

        @Size(max = 512, message = "썸네일 URL은 512자 이하여야 합니다.")
        String thumbnailUrl,

        // WALK(기본), DRIVE, BIKE, TRANSIT
        @Pattern(regexp = "^$|^(?i)(WALK|DRIVE|BIKE|TRANSIT)$",
                message = "코스 유형은 WALK, DRIVE, BIKE, TRANSIT 중 하나여야 합니다.")
        String type,

        @NotEmpty(message = "코스에 담을 장소를 1개 이상 선택해야 합니다.")
        List<@NotNull(message = "장소 ID는 비어 있을 수 없습니다.") Long> placeIds
) {
    public String typeOrDefault() {
        return (type == null || type.isBlank()) ? "WALK" : type.toUpperCase();
    }
}
