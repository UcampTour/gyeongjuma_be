package com.ucamp.gyeongjuma_be.mypage.service;

import com.ucamp.gyeongjuma_be.common.exception.CustomException;
import com.ucamp.gyeongjuma_be.common.exception.ErrorCode;
import com.ucamp.gyeongjuma_be.member.domain.Member;
import com.ucamp.gyeongjuma_be.member.repository.MemberRepository;
import com.ucamp.gyeongjuma_be.mypage.dto.response.CourseProgressDto;
import com.ucamp.gyeongjuma_be.mypage.dto.response.CourseProgressListResponse;
import com.ucamp.gyeongjuma_be.mypage.dto.response.MyPageInfoResponse;
import com.ucamp.gyeongjuma_be.mypage.dto.response.StampDto;
import com.ucamp.gyeongjuma_be.mypage.dto.response.StampListResponse;
import com.ucamp.gyeongjuma_be.mypage.dto.response.VisitHistoryDto;
import com.ucamp.gyeongjuma_be.mypage.dto.response.VisitHistoryListResponse;
import com.ucamp.gyeongjuma_be.mypage.repository.MyPageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageServiceImpl implements MyPageService {

    private final MemberRepository memberRepository;
    private final MyPageRepository myPageRepository;

    @Override
    public MyPageInfoResponse getMyInfo(Long memberId) {
        Member member = getActiveMember(memberId);

        return MyPageInfoResponse.builder()
                .memberId(member.getMemberId())
                .nickname(member.getNickname())
                .profileImage(member.getProfileImgUrl())
                .difficulty(member.getDifficulty())
                .point(member.getPoint())
                .distance(member.getDistance())
                .visitPlaceCnt(myPageRepository.countVisitedPlacesByMemberId(memberId))
                .build();
    }

    @Override
    public VisitHistoryListResponse getVisitHistory(Long memberId) {
        getActiveMember(memberId);

        List<VisitHistoryDto> visits = myPageRepository.findVisitHistoryByMemberId(memberId);

        return VisitHistoryListResponse.builder()
                .totalCnt(visits.size())
                .visits(visits)
                .build();
    }

    @Override
    public StampListResponse getStamps(Long memberId) {
        getActiveMember(memberId);

        List<StampDto> stamps = myPageRepository.findStampsByMemberId(memberId);
        int acquiredCnt = (int) stamps.stream()
                .filter(stamp -> Boolean.TRUE.equals(stamp.getIsAcquired()))
                .count();

        return StampListResponse.builder()
                .totalCnt(stamps.size())
                .acquiredCnt(acquiredCnt)
                .stamps(stamps)
                .build();
    }

    @Override
    public CourseProgressListResponse getCourseProgress(Long memberId) {
        getActiveMember(memberId);

        List<CourseProgressDto> courses = myPageRepository.findCourseProgressByMemberId(memberId);
        int completedCnt = (int) courses.stream()
                .filter(course -> Boolean.TRUE.equals(course.getIsCompleted()))
                .count();

        return CourseProgressListResponse.builder()
                .totalCnt(courses.size())
                .completedCnt(completedCnt)
                .courses(courses)
                .build();
    }

    private Member getActiveMember(Long memberId) {
        Member member = memberRepository.findById(memberId);
        if (member == null) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }
        return member;
    }
}
