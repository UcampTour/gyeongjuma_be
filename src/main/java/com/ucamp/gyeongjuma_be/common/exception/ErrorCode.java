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

    // Place
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "장소를 찾을 수 없습니다."),
    OUT_OF_VISIT_RADIUS(HttpStatus.BAD_REQUEST, "P002", "방문 인증 가능 반경 밖입니다."),
    ALREADY_VISITED(HttpStatus.BAD_REQUEST, "P003", "이미 방문 인증한 장소입니다."),

    // Database / Infrastructure
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "D001", "데이터베이스 처리 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
