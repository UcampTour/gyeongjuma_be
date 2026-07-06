package com.ucamp.gyeongjuma_be.member.dto.response;

public record NicknameCheckResponse(
        String nickname,
        boolean available
) {
}
