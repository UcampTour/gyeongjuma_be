package com.ucamp.gyeongjuma_be.member.domain;

import com.ucamp.gyeongjuma_be.common.domain.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Member extends BaseEntity {

    private Long memberId;
    private String provider;       // GOOGLE, KAKAO, ...
    private String providerId;     // 소셜 제공자가 발급한 고유 ID (sub)
    private String nickname;
    private String profileImgUrl;
    private String locale;
    private String difficulty;     // 퀴즈 난이도 (EASY, NORMAL, HARD)
    private Long point;
    private Long distance;
    private Long visitPlaceCnt;
    private String refreshToken;
    private LocalDateTime refreshTokenExpiredAt;

    @Builder
    public Member(String provider, String providerId, String nickname,
                  String profileImgUrl, String locale, String difficulty) {
        this.provider = provider;
        this.providerId = providerId;
        this.nickname = nickname;
        this.profileImgUrl = profileImgUrl;
        this.locale = locale;
        this.difficulty = difficulty;
    }
}
