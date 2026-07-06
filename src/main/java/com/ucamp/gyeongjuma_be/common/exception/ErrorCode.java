package com.ucamp.gyeongjuma_be.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common (공통)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "올바르지 않은 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 오류가 발생했습니다."),

    // Member (회원 관련 예시)
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "존재하지 않는 회원 정보입니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "M002", "이미 가입된 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "M003", "이미 사용 중인 닉네임입니다."),
    ALREADY_WITHDRAWN_MEMBER(HttpStatus.BAD_REQUEST, "M004", "이미 탈퇴한 회원입니다."),

    // Auth (인증/인가)
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "만료된 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A004", "유효하지 않은 리프레시 토큰입니다."),
    SOCIAL_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "A005", "소셜 로그인 인증에 실패했습니다."),

    // Database / Infrastructure
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "D001", "데이터베이스 처리 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}