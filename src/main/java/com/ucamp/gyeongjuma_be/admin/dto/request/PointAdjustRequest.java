package com.ucamp.gyeongjuma_be.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 포인트 조정. amount는 증감분(음수 가능)이며 최종 포인트가 음수가 되지 않도록 서비스에서 막는다.
 */
public record PointAdjustRequest(
        @NotNull(message = "조정할 포인트(amount)는 필수입니다.")
        Long amount,

        @Size(max = 100, message = "사유는 100자 이내여야 합니다.")
        String reason
) {
    /**
     * 사유 기본값. 실제 이력에는 서비스가 여기에 조정 시각을 덧붙여 저장한다
     * (point_history의 (member_id, description) 유니크 제약 때문에 같은 문구를 반복 저장할 수 없다).
     */
    public String reasonOrDefault() {
        return (reason == null || reason.isBlank()) ? "관리자 조정" : reason;
    }
}
