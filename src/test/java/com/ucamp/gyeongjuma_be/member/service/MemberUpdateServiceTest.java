package com.ucamp.gyeongjuma_be.member.service;

import com.ucamp.gyeongjuma_be.auth.jwt.JwtTokenProvider;
import com.ucamp.gyeongjuma_be.common.exception.CustomException;
import com.ucamp.gyeongjuma_be.common.exception.ErrorCode;
import com.ucamp.gyeongjuma_be.member.domain.Member;
import com.ucamp.gyeongjuma_be.member.dto.request.MemberUpdateRequest;
import com.ucamp.gyeongjuma_be.member.dto.response.MemberInfoResponse;
import com.ucamp.gyeongjuma_be.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberServiceImpl.updateMyInfo - 닉네임/난이도/언어 부분 수정")
class MemberUpdateServiceTest {

    @Mock
    MemberRepository memberRepository;
    @Mock
    JwtTokenProvider jwtTokenProvider;
    @Mock
    TransactionTemplate transactionTemplate;

    @InjectMocks
    MemberServiceImpl memberService;

    private Member existingMember() {
        Member member = new Member();
        member.setMemberId(1L);
        member.setNickname("기존닉네임");
        member.setProfileImgUrl("https://img.example/1.png");
        member.setDifficulty("HARD");
        member.setLocale("en");
        return member;
    }

    @Test
    @DisplayName("난이도만 보내면 닉네임·언어는 기존 값이 유지된다")
    void updatesOnlyDifficulty() {
        given(memberRepository.findById(1L)).willReturn(existingMember());

        MemberInfoResponse response =
                memberService.updateMyInfo(1L, new MemberUpdateRequest(null, "easy", null));

        // 닉네임을 안 보냈으므로 중복 검사도 하지 않아야 한다
        verify(memberRepository, never()).existsByNickname(any());
        // null 필드는 매퍼 <if>에서 제외되도록 null 그대로 넘어가야 한다
        verify(memberRepository).updateProfile(eq(1L), eq(null), eq("EASY"), eq(null), any(LocalDateTime.class));

        assertThat(response.nickname()).isEqualTo("기존닉네임");
        assertThat(response.difficulty()).isEqualTo("EASY");   // 대문자로 정규화
        assertThat(response.locale()).isEqualTo("en");         // 덮어쓰이지 않음
        assertThat(response.profileImage()).isEqualTo("https://img.example/1.png");
    }

    @Test
    @DisplayName("언어만 보내면 소문자로 정규화되고 난이도는 유지된다")
    void updatesOnlyLocale() {
        given(memberRepository.findById(1L)).willReturn(existingMember());

        MemberInfoResponse response =
                memberService.updateMyInfo(1L, new MemberUpdateRequest(null, null, "KO"));

        verify(memberRepository).updateProfile(eq(1L), eq(null), eq(null), eq("ko"), any(LocalDateTime.class));
        assertThat(response.locale()).isEqualTo("ko");
        assertThat(response.difficulty()).isEqualTo("HARD");
    }

    @Test
    @DisplayName("닉네임을 바꾸면 중복 검사를 거친다")
    void checksNicknameDuplication() {
        given(memberRepository.findById(1L)).willReturn(existingMember());
        given(memberRepository.existsByNickname("새닉네임")).willReturn(false);

        MemberInfoResponse response =
                memberService.updateMyInfo(1L, new MemberUpdateRequest("새닉네임", null, null));

        verify(memberRepository).existsByNickname("새닉네임");
        assertThat(response.nickname()).isEqualTo("새닉네임");
    }

    @Test
    @DisplayName("본인이 쓰던 닉네임을 그대로 보내면 중복 검사를 건너뛴다")
    void skipsDuplicationCheckForOwnNickname() {
        given(memberRepository.findById(1L)).willReturn(existingMember());

        memberService.updateMyInfo(1L, new MemberUpdateRequest("기존닉네임", null, null));

        verify(memberRepository, never()).existsByNickname(any());
    }

    @Test
    @DisplayName("남이 쓰는 닉네임이면 DUPLICATE_NICKNAME")
    void throwsOnDuplicateNickname() {
        given(memberRepository.findById(1L)).willReturn(existingMember());
        given(memberRepository.existsByNickname("남의닉네임")).willReturn(true);

        assertThatThrownBy(() ->
                memberService.updateMyInfo(1L, new MemberUpdateRequest("남의닉네임", null, null)))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_NICKNAME);

        verify(memberRepository, never())
                .updateProfile(anyLong(), any(), any(), any(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("세 필드 모두 비어 있으면 INVALID_INPUT_VALUE")
    void throwsOnEmptyRequest() {
        given(memberRepository.findById(1L)).willReturn(existingMember());

        assertThatThrownBy(() ->
                memberService.updateMyInfo(1L, new MemberUpdateRequest(null, null, null)))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);

        verify(memberRepository, never())
                .updateProfile(anyLong(), any(), any(), any(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("존재하지 않는 회원이면 MEMBER_NOT_FOUND")
    void throwsWhenMemberNotFound() {
        given(memberRepository.findById(999L)).willReturn(null);

        assertThatThrownBy(() ->
                memberService.updateMyInfo(999L, new MemberUpdateRequest("아무닉네임", null, null)))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("updated_at은 KST 기준 시각으로 넘어간다")
    void passesKstTimestamp() {
        given(memberRepository.findById(1L)).willReturn(existingMember());
        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);

        memberService.updateMyInfo(1L, new MemberUpdateRequest(null, "NORMAL", null));

        verify(memberRepository).updateProfile(eq(1L), eq(null), eq("NORMAL"), eq(null), captor.capture());
        LocalDateTime kstNow = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        assertThat(captor.getValue())
                .isBetween(kstNow.minusMinutes(1), kstNow.plusMinutes(1));
        // UTC로 넣었다면 9시간 어긋난다 — KST가 맞는지 확인
        assertThat(captor.getValue())
                .isAfter(LocalDateTime.now(ZoneId.of("UTC")).plusHours(8));
    }
}
