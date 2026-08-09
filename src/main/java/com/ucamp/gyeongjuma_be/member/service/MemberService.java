package com.ucamp.gyeongjuma_be.member.service;

import com.ucamp.gyeongjuma_be.member.dto.request.ExtraInfoRequest;
import com.ucamp.gyeongjuma_be.member.dto.request.LoginRequest;
import com.ucamp.gyeongjuma_be.member.dto.response.LoginResult;
import com.ucamp.gyeongjuma_be.member.dto.response.MemberInfoResponse;
import com.ucamp.gyeongjuma_be.member.dto.response.NicknameCheckResponse;
import com.ucamp.gyeongjuma_be.member.dto.response.TokenResult;

public interface MemberService {

    LoginResult login(LoginRequest request);

    MemberInfoResponse getMyInfo(Long memberId);

    MemberInfoResponse registerExtraInfo(Long memberId, ExtraInfoRequest request);

    NicknameCheckResponse checkNickname(String nickname);

    TokenResult reissue(String refreshToken);

    void logout(Long memberId);

    void withdraw(Long memberId);
}
