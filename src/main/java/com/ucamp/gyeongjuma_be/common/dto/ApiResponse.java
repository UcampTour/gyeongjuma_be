package com.ucamp.gyeongjuma_be.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.time.ZoneId;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        Status status,
        String message,
        T data,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp
) {
    // ✅ 응답 상태 정의
    public enum Status {
        SUCCESS, ERROR
    }

    // ✅ 성공 응답 (데이터 없음)
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(Status.SUCCESS, message, null, LocalDateTime.now(ZoneId.of("Asia/Seoul")));
    }

    // ✅ 성공 응답 (데이터 포함)
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(Status.SUCCESS, message, data, LocalDateTime.now(ZoneId.of("Asia/Seoul")));
    }

    // ✅ 실패 응답 (데이터 없음)
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(Status.ERROR, message, null, LocalDateTime.now(ZoneId.of("Asia/Seoul")));
    }

    // ✅ 실패 응답 (데이터 포함)
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(Status.ERROR, message, data, LocalDateTime.now(ZoneId.of("Asia/Seoul")));
    }
}