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
    public String reasonOrDefault() {
        return (reason == null || reason.isBlank()) ? "관리자 조정" : reason;
    }
}
