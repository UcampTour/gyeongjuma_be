-- =========================================================
-- 회원(member) 테이블 DDL — 실제 운영 테이블 구조 기준 (참고용)
-- 이미 DB에 테이블이 존재하므로 실행할 필요 없음.
-- 새 환경 세팅 시에만 사용.
-- =========================================================

CREATE TABLE IF NOT EXISTS member (
    member_id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '회원 ID',
    provider                 VARCHAR(20)  NOT NULL COMMENT '소셜 제공자 (GOOGLE/KAKAO 등)',
    provider_id              VARCHAR(100) NOT NULL COMMENT '소셜 제공자 발급 고유 ID (sub)',
    nickname                 VARCHAR(30)  NULL COMMENT '닉네임 (추가 정보 등록 시 설정)',
    profile_img_url          VARCHAR(500) NULL COMMENT '프로필 이미지 URL',
    locale                   VARCHAR(10)  NULL DEFAULT 'ko' COMMENT '언어',
    difficulty               VARCHAR(20)  NOT NULL DEFAULT 'EASY' COMMENT '퀴즈 난이도 (EASY/NORMAL/HARD)',
    `point`                  BIGINT       NOT NULL DEFAULT 0 COMMENT '포인트',
    distance                 BIGINT       NOT NULL DEFAULT 0 COMMENT '이동 거리',
    visit_place_cnt          BIGINT       NOT NULL DEFAULT 0 COMMENT '방문 장소 수',
    refresh_token            VARCHAR(512) NULL COMMENT '리프레시 토큰',
    refresh_token_expired_at DATETIME     NULL COMMENT '리프레시 토큰 만료 시각',
    created_at               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at               DATETIME     NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    deleted_at               DATETIME     NULL COMMENT '탈퇴일시',
    is_active                BOOLEAN      NOT NULL DEFAULT TRUE COMMENT '활성 여부 (탈퇴 시 FALSE)',
    PRIMARY KEY (member_id),
    UNIQUE KEY uk_member_provider (provider, provider_id),
    KEY idx_member_nickname (nickname)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '회원';
