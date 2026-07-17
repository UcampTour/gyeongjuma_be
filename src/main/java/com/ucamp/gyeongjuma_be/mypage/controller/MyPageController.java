package com.ucamp.gyeongjuma_be.mypage.controller;

import com.ucamp.gyeongjuma_be.auth.AuthInterceptor;
import com.ucamp.gyeongjuma_be.common.dto.ApiResponse;
import com.ucamp.gyeongjuma_be.mypage.dto.response.CourseProgressListResponse;
import com.ucamp.gyeongjuma_be.mypage.dto.response.MyPageInfoResponse;
import com.ucamp.gyeongjuma_be.mypage.dto.response.StampListResponse;
import com.ucamp.gyeongjuma_be.mypage.dto.response.VisitHistoryListResponse;
import com.ucamp.gyeongjuma_be.mypage.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    /**
     * 1. 마이페이지 회원 정보 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<MyPageInfoResponse>> getMyInfo(
            @RequestAttribute(AuthInterceptor.MEMBER_ID_ATTRIBUTE) Long memberId) {
        MyPageInfoResponse response = myPageService.getMyInfo(memberId);
        return ResponseEntity.ok(ApiResponse.success("회원 정보 조회에 성공했습니다.", response));
    }

    /**
     * 2. 발자취 목록 탭 조회 (방문한 장소 이력)
     */
    @GetMapping("/visits")
    public ResponseEntity<ApiResponse<VisitHistoryListResponse>> getVisitHistory(
            @RequestAttribute(AuthInterceptor.MEMBER_ID_ATTRIBUTE) Long memberId) {
        VisitHistoryListResponse response = myPageService.getVisitHistory(memberId);
        return ResponseEntity.ok(ApiResponse.success("발자취 목록 조회에 성공했습니다.", response));
    }

    /**
     * 3. 스탬프 목록 탭 조회 (전체 스탬프 + 획득 여부)
     */
    @GetMapping("/stamps")
    public ResponseEntity<ApiResponse<StampListResponse>> getStamps(
            @RequestAttribute(AuthInterceptor.MEMBER_ID_ATTRIBUTE) Long memberId) {
        StampListResponse response = myPageService.getStamps(memberId);
        return ResponseEntity.ok(ApiResponse.success("스탬프 목록 조회에 성공했습니다.", response));
    }

    /**
     * 4. 진행도 목록 탭 조회 (코스별 진행 상황)
     */
    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<CourseProgressListResponse>> getCourseProgress(
            @RequestAttribute(AuthInterceptor.MEMBER_ID_ATTRIBUTE) Long memberId) {
        CourseProgressListResponse response = myPageService.getCourseProgress(memberId);
        return ResponseEntity.ok(ApiResponse.success("진행도 목록 조회에 성공했습니다.", response));
    }
}
