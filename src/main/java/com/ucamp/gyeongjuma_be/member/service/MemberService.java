package com.ucamp.gyeongjuma_be.member.service;

import com.ucamp.gyeongjuma_be.member.dto.request.ExtraInfoRequest;
import com.ucamp.gyeongjuma_be.member.dto.request.LoginRequest;
import com.ucamp.gyeongjuma_be.member.dto.response.LoginResponse;
import com.ucamp.gyeongjuma_be.member.dto.response.MemberInfoResponse;
import com.ucamp.gyeongjuma_be.member.dto.response.NicknameCheckResponse;
import com.ucamp.gyeongjuma_be.member.dto.response.TokenResponse;

public interface MemberService {

    LoginResponse login(LoginRequest request);

    MemberInfoResponse registerExtraInfo(Long memberId, ExtraInfoRequest request);

    NicknameCheckResponse checkNickname(String nickname);

    TokenResponse reissue(String refreshToken);

    void logout(Long memberId);

    void withdraw(Long memberId);
}
