package com.ucamp.gyeongjuma_be.auth.oauth;

/**
 * 소셜 제공자로부터 받은 공통 사용자 정보
 */
public record SocialUserInfo(
        String providerId,
        String email,
        String name,
        String profileImage
) {
}
