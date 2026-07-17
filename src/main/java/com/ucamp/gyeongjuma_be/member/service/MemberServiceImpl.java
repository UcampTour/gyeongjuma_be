package com.ucamp.gyeongjuma_be.member.service;

import com.ucamp.gyeongjuma_be.auth.jwt.JwtTokenProvider;
import com.ucamp.gyeongjuma_be.auth.oauth.SocialTokenVerifier;
import com.ucamp.gyeongjuma_be.auth.oauth.SocialUserInfo;
import com.ucamp.gyeongjuma_be.common.exception.CustomException;
import com.ucamp.gyeongjuma_be.common.exception.ErrorCode;
import com.ucamp.gyeongjuma_be.member.domain.Member;
import com.ucamp.gyeongjuma_be.member.dto.request.ExtraInfoRequest;
import com.ucamp.gyeongjuma_be.member.dto.request.LoginRequest;
import com.ucamp.gyeongjuma_be.member.dto.response.LoginResponse;
import com.ucamp.gyeongjuma_be.member.dto.response.MemberInfoResponse;
import com.ucamp.gyeongjuma_be.member.dto.response.NicknameCheckResponse;
import com.ucamp.gyeongjuma_be.member.dto.response.TokenResponse;
import com.ucamp.gyeongjuma_be.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final List<SocialTokenVerifier> socialTokenVerifiers;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String provider = request.providerOrDefault();

        SocialTokenVerifier verifier = socialTokenVerifiers.stream()
                .filter(v -> v.provider().equals(provider))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED));

        String token = request.resolveToken();
        if (token == null || token.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        SocialUserInfo userInfo = verifier.verify(token);

        Member member = memberRepository.findByProviderAndProviderId(provider, userInfo.providerId());
        boolean isNewMember = (member == null);

        // 탈퇴했던 회원이 다시 로그인하면 재활성화하되, 신규 회원처럼 추가 정보를 다시 받는다
        if (member != null && Boolean.FALSE.equals(member.getIsActive())) {
            memberRepository.reactivateMember(member.getMemberId());
            member.setNickname(null);
        }

        if (isNewMember) {
            member = Member.builder()
                    .provider(provider)
                    .providerId(userInfo.providerId())
                    .profileImgUrl(userInfo.profileImage() != null ? userInfo.profileImage() : "")
                    .locale("ko")
                    .difficulty("NORMAL")
                    .build();
            memberRepository.insertMember(member);
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getMemberId());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getMemberId());
        memberRepository.updateRefreshToken(member.getMemberId(), refreshToken,
                jwtTokenProvider.getRefreshTokenExpiredAt());

        // 닉네임(추가 정보)을 아직 등록하지 않았다면 신규 회원으로 취급
        return LoginResponse.builder()
                .memberId(member.getMemberId())
                .email(userInfo.email())
                .nickname(member.getNickname())
                .isNewMember(isNewMember || member.getNickname() == null)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public MemberInfoResponse getMyInfo(Long memberId) {
        Member member = getActiveMember(memberId);

        return MemberInfoResponse.builder()
                .memberId(member.getMemberId())
                .nickname(member.getNickname())
                .profileImage(member.getProfileImgUrl())
                .difficulty(member.getDifficulty())
                .locale(member.getLocale())
                .build();
    }

    @Override
    @Transactional
    public MemberInfoResponse registerExtraInfo(Long memberId, ExtraInfoRequest request) {
        Member member = getActiveMember(memberId);

        // 본인이 이미 쓰고 있는 닉네임이 아니라면 중복 검사
        if (!request.nickname().equals(member.getNickname())
                && memberRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        memberRepository.updateExtraInfo(memberId, request.nickname(),
                request.difficultyOrDefault(), request.localeOrDefault());

        return MemberInfoResponse.builder()
                .memberId(memberId)
                .nickname(request.nickname())
                .profileImage(member.getProfileImgUrl())
                .difficulty(request.difficultyOrDefault())
                .locale(request.localeOrDefault())
                .build();
    }

    @Override
    public NicknameCheckResponse checkNickname(String nickname) {
        boolean available = !memberRepository.existsByNickname(nickname);
        return new NicknameCheckResponse(nickname, available);
    }

    @Override
    @Transactional
    public TokenResponse reissue(String refreshToken) {
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long memberId = jwtTokenProvider.getMemberId(refreshToken);
        Member member = getActiveMember(memberId);

        // DB에 저장된 리프레시 토큰과 일치해야 함 (로그아웃/탈취 방지)
        if (member.getRefreshToken() == null || !member.getRefreshToken().equals(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(memberId);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(memberId);
        memberRepository.updateRefreshToken(memberId, newRefreshToken,
                jwtTokenProvider.getRefreshTokenExpiredAt());

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Override
    @Transactional
    public void logout(Long memberId) {
        getActiveMember(memberId);
        memberRepository.updateRefreshToken(memberId, null, null);
    }

    @Override
    @Transactional
    public void withdraw(Long memberId) {
        getActiveMember(memberId);
        memberRepository.softDeleteById(memberId);
    }

    private Member getActiveMember(Long memberId) {
        Member member = memberRepository.findById(memberId);
        if (member == null) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }
        return member;
    }
}
