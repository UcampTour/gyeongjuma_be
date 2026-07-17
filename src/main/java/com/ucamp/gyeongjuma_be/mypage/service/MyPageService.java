package com.ucamp.gyeongjuma_be.mypage.service;

import com.ucamp.gyeongjuma_be.mypage.dto.response.CourseProgressListResponse;
import com.ucamp.gyeongjuma_be.mypage.dto.response.MyPageInfoResponse;
import com.ucamp.gyeongjuma_be.mypage.dto.response.StampListResponse;
import com.ucamp.gyeongjuma_be.mypage.dto.response.VisitHistoryListResponse;

public interface MyPageService {

    MyPageInfoResponse getMyInfo(Long memberId);

    VisitHistoryListResponse getVisitHistory(Long memberId);

    StampListResponse getStamps(Long memberId);

    CourseProgressListResponse getCourseProgress(Long memberId);
}
