package com.ucamp.gyeongjuma_be.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.SQLIntegrityConstraintViolationException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler - 유니크 제약 위반 변환")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("uk_member_nickname 위반이면 409 M003으로 변환한다")
    void mapsNicknameUniqueViolationToConflict() {
        DuplicateKeyException e = new DuplicateKeyException(
                "PreparedStatementCallback; SQL [UPDATE member ...]",
                new SQLIntegrityConstraintViolationException(
                        "Duplicate entry '불국사덕후' for key 'member.uk_member_nickname'"));

        ResponseEntity<ErrorResponse> response = handler.handleDuplicateKeyException(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.DUPLICATE_NICKNAME.getCode());
        assertThat(response.getBody().getMessage()).isEqualTo("이미 사용 중인 닉네임입니다.");
    }

    @Test
    @DisplayName("다른 테이블의 유니크 제약 위반은 닉네임 중복으로 오인하지 않는다")
    void doesNotMisreportOtherUniqueViolations() {
        DuplicateKeyException e = new DuplicateKeyException(
                "PreparedStatementCallback; SQL [INSERT INTO member_stamp ...]",
                new SQLIntegrityConstraintViolationException(
                        "Duplicate entry '1-7' for key 'member_stamp.uk_member_stamp'"));

        ResponseEntity<ErrorResponse> response = handler.handleDuplicateKeyException(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.DATABASE_ERROR.getCode());
    }

    @Test
    @DisplayName("원인 예외가 없어도 NPE 없이 처리된다")
    void handlesExceptionWithoutCause() {
        ResponseEntity<ErrorResponse> response =
                handler.handleDuplicateKeyException(new DuplicateKeyException("원인 불명"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.DATABASE_ERROR.getCode());
    }
}
