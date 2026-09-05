package com.ucamp.gyeongjuma_be.admin.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 관리자 회원 수정. 닉네임 변경과 포인트 조정을 한 요청으로 처리한다.
 * 모든 필드가 선택값이며, 보내지 않은 항목은 건드리지 않는다.
 */
public record MemberUpdateAdminRequest(
        @Size(min = 2, max = 12, message = "닉네임은 2~12자여야 합니다.")
        @Pattern(regexp = "^[가-힣a-zA-Z0-9]+$", message = "닉네임은 한글, 영문, 숫자만 사용할 수 있습니다.")
        String nickname,

        /** 포인트 증감분. 음수 가능하며 최종 포인트가 음수가 되면 거부한다. */
        Long pointAmount,

        @Size(max = 100, message = "사유는 100자 이내여야 합니다.")
        String reason
) {
    public boolean isEmpty() {
        return nickname == null && pointAmount == null;
    }

    public boolean hasPointChange() {
        return pointAmount != null && pointAmount != 0L;
    }

    public String reasonOrDefault() {
        return (reason == null || reason.isBlank()) ? "관리자 조정" : reason;
    }
}
