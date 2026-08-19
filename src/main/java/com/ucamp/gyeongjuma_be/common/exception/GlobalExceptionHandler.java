package com.ucamp.gyeongjuma_be.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice // 💡 @ControllerAdvice + @ResponseBody 결합, 모든 API 컨트롤러의 예외를 감지
public class GlobalExceptionHandler {

    /** member.nickname 유니크 인덱스명 — 이 제약 위반만 닉네임 중복(409)으로 변환한다 */
    private static final String MEMBER_NICKNAME_UNIQUE_KEY = "uk_member_nickname";

    /**
     * 1. 개발자가 의도적으로 던진 비즈니스 커스텀 예외 처리
     */
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.error("CustomException 발생: {}", e.getErrorCode().getMessage());
        ErrorCode errorCode = e.getErrorCode();

        ErrorResponse response = ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .error(errorCode.getStatus().name())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    /**
     * 2. @Valid 또는 @Validated 사용 시 입력값 검증(Validation) 실패 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException 발생", e);
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;

        // 유효성 검사 실패 메시지 중 첫 번째 항목을 가져옴
        String bindingMessage = e.getBindingResult().getFieldError() != null ?
                e.getBindingResult().getFieldError().getDefaultMessage() : errorCode.getMessage();

        ErrorResponse response = ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .error(errorCode.getStatus().name())
                .code(errorCode.getCode())
                .message(bindingMessage)
                .build();

        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    /**
     * 3. 지원하지 않는 HTTP Method 호출 시 발생 (예: POST로 설계된 API를 GET으로 찌를 때)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("HttpRequestMethodNotSupportedException 발생", e);
        ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;

        ErrorResponse response = ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .error(errorCode.getStatus().name())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    /**
     * 3-1. 유니크 제약 위반 처리.
     * 닉네임은 애플리케이션에서 먼저 중복 검사를 하지만, 검사와 저장 사이에 다른 요청이 끼어들면
     * DB 제약(uk_member_nickname)에서 걸린다. 이때 500(D001) 대신 409(M003)로 내려준다.
     * 다른 테이블의 유니크 제약 위반까지 닉네임 중복으로 오인하지 않도록 인덱스명으로 분기한다.
     */
    @ExceptionHandler(DuplicateKeyException.class)
    protected ResponseEntity<ErrorResponse> handleDuplicateKeyException(DuplicateKeyException e) {
        String detail = e.getMostSpecificCause().getMessage();
        ErrorCode errorCode = (detail != null && detail.contains(MEMBER_NICKNAME_UNIQUE_KEY))
                ? ErrorCode.DUPLICATE_NICKNAME
                : ErrorCode.DATABASE_ERROR;
        log.error("DuplicateKeyException 발생 → {} 로 변환", errorCode.getCode(), e);

        ErrorResponse response = ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .error(errorCode.getStatus().name())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    /**
     * 4. MyBatis / SQL 실행 중 데이터베이스 관련 예외 발생 시 처리
     */
    @ExceptionHandler(DataAccessException.class)
    protected ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException e) {
        log.error("DataAccessException (DB 오류) 발생", e);
        ErrorCode errorCode = ErrorCode.DATABASE_ERROR;

        ErrorResponse response = ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .error(errorCode.getStatus().name())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    /**
     * 5. 존재하지 않는 정적 리소스/경로 요청 (favicon.ico 등) — 스택 트레이스 없이 404만 반환
     */
    @ExceptionHandler(NoResourceFoundException.class)
    protected ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("존재하지 않는 리소스 요청: {}", e.getResourcePath());

        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.name())
                .code("C404")
                .message("요청한 리소스를 찾을 수 없습니다.")
                .build();

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * 6. 위에 정의되지 않은 그 외의 모든 예상치 못한 최상위 예외(Exception.class) 처리
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("예상치 못한 Exception 발생", e);
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        ErrorResponse response = ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .error(errorCode.getStatus().name())
                .code(errorCode.getCode())
                .message(e.getMessage() != null ? e.getMessage() : errorCode.getMessage())
                .build();

        return new ResponseEntity<>(response, errorCode.getStatus());
    }
}