package com.ucamp.gyeongjuma_be.mypage.dto.response;

import lombok.Builder;

@Builder
public record MyPageInfoResponse(
        Long memberId,
        String nickname,
        String profileImage,
        String difficulty,
        Long point,
        /** 누적 포인트 — member.point와 같은 값 (프론트 요청 필드명) */
        Long totalPoint,
        Long distance,
        Long visitPlaceCnt,
        /** 모든 문항을 푼 퀴즈 세트 수 */
        Long quizCount,
        /** 담긴 장소를 모두 방문한 코스 수 */
        Long courseCount
) {
}
