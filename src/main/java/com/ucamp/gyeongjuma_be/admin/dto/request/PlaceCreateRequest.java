package com.ucamp.gyeongjuma_be.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 관광지 수동 등록. 좌표는 지도 표시·방문 인증에 쓰이므로 값이 없으면 0으로 저장한다.
 */
public record PlaceCreateRequest(
        @NotBlank(message = "관광지명은 필수입니다.")
        @Size(max = 255, message = "관광지명은 255자 이내여야 합니다.")
        String placeName,

        @NotBlank(message = "주소는 필수입니다.")
        @Size(max = 255, message = "주소는 255자 이내여야 합니다.")
        String add1,

        @Size(max = 255, message = "상세주소는 255자 이내여야 합니다.")
        String add2,

        @Size(max = 50, message = "전화번호는 50자 이내여야 합니다.")
        String tel,

        Long contentTypeId,
        Double mapX,
        Double mapY,

        @Size(max = 512, message = "이미지 URL은 512자 이내여야 합니다.")
        String firstImage,

        Long radiusMeters
) {
    public String add2OrEmpty() {
        return add2 == null ? "" : add2;
    }

    public String telOrEmpty() {
        return tel == null ? "" : tel;
    }

    public String firstImageOrEmpty() {
        return firstImage == null ? "" : firstImage;
    }

    public Long contentTypeIdOrDefault() {
        return contentTypeId == null ? 12L : contentTypeId;
    }

    public Double mapXOrZero() {
        return mapX == null ? 0.0 : mapX;
    }

    public Double mapYOrZero() {
        return mapY == null ? 0.0 : mapY;
    }

    /** 방문 인증 반경. 미지정 시 100m */
    public Long radiusMetersOrDefault() {
        return radiusMeters == null ? 100L : radiusMeters;
    }
}
