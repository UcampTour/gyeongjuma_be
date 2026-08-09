package com.ucamp.gyeongjuma_be.admin.service;

import com.ucamp.gyeongjuma_be.admin.dto.request.AdminLoginRequest;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminLoginResult;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminMemberDetailDto;
import com.ucamp.gyeongjuma_be.admin.repository.AdminMemberRepository;
import com.ucamp.gyeongjuma_be.auth.jwt.JwtTokenProvider;
import com.ucamp.gyeongjuma_be.common.exception.CustomException;
import com.ucamp.gyeongjuma_be.common.exception.ErrorCode;
import com.ucamp.gyeongjuma_be.member.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자 마스터 계정 로그인.
 *
 * 아이디와 비밀번호 해시는 application-secret.properties에서 주입받는다.
 * 비밀번호는 BCrypt 해시로만 저장하며 평문은 어디에도 두지 않는다.
 * 해시 생성 방법은 sql/admin_master_account.sql 주석 참고.
 */
@Slf4j
@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    /** 마스터 계정을 식별하는 member 행의 provider/provider_id */
    public static final String MASTER_PROVIDER = "ADMIN";
    public static final String MASTER_PROVIDER_ID = "master";

    private static final String ROLE_ADMIN = "ADMIN";

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final AdminMemberRepository adminMemberRepository;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    private final String masterUsername;
    private final String masterPasswordHash;

    public AdminAuthServiceImpl(AdminMemberRepository adminMemberRepository,
                                MemberRepository memberRepository,
                                JwtTokenProvider jwtTokenProvider,
                                @Value("${admin.master.username:}") String masterUsername,
                                @Value("${admin.master.password-hash:}") String masterPasswordHash) {
        this.adminMemberRepository = adminMemberRepository;
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.masterUsername = masterUsername;
        this.masterPasswordHash = masterPasswordHash;
    }

    @Override
    @Transactional
    public AdminLoginResult login(AdminLoginRequest request) {
        // 설정이 비어 있으면 마스터 로그인 자체를 막는다 (빈 비밀번호로 뚫리는 것 방지)
        if (masterUsername.isBlank() || masterPasswordHash.isBlank()) {
            log.error("관리자 마스터 계정이 설정되지 않았습니다. admin.master.username / admin.master.password-hash 확인 필요");
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        boolean usernameMatches = masterUsername.equals(request.username());
        boolean passwordMatches = passwordEncoder.matches(request.password(), masterPasswordHash);

        // 아이디가 틀렸는지 비밀번호가 틀렸는지 구분해서 알려주지 않는다
        if (!usernameMatches || !passwordMatches) {
            log.warn("관리자 로그인 실패. username={}", request.username());
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        AdminMemberDetailDto master = adminMemberRepository.findMasterAdmin(MASTER_PROVIDER, MASTER_PROVIDER_ID);
        if (master == null) {
            log.error("마스터 계정 회원 행이 없습니다. sql/admin_master_account.sql 실행 필요");
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }
        if (!ROLE_ADMIN.equals(master.getRole())) {
            log.error("마스터 계정의 role이 ADMIN이 아닙니다. memberId={}", master.getMemberId());
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        String accessToken = jwtTokenProvider.createAccessToken(master.getMemberId());
        String refreshToken = jwtTokenProvider.createRefreshToken(master.getMemberId());
        memberRepository.updateRefreshToken(master.getMemberId(), refreshToken,
                jwtTokenProvider.getRefreshTokenExpiredAt());

        return AdminLoginResult.builder()
                .memberId(master.getMemberId())
                .username(masterUsername)
                .nickname(master.getNickname())
                .role(master.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
