package com.ucamp.gyeongjuma_be.member.controller;

import com.ucamp.gyeongjuma_be.auth.jwt.JwtTokenProvider;
import com.ucamp.gyeongjuma_be.member.domain.Member;
import com.ucamp.gyeongjuma_be.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PATCH /api/members/me 통합 테스트.
 * 공유 DB이므로 @Transactional 롤백 안에서 데모 회원을 만들고, 그 회원 ID로만 단언한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("PATCH /api/members/me 통합 테스트")
class MemberUpdateIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    JwtTokenProvider jwtTokenProvider;

    private Long memberId;
    private String accessToken;
    private String originalNickname;

    @BeforeEach
    void setUp() {
        // 테스트 식별자에 UUID를 붙여 팀원 데이터와 충돌하지 않게 한다
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        originalNickname = "테스트" + suffix;

        Member member = Member.builder()
                .provider("GOOGLE")
                .providerId("test-" + suffix)
                .nickname(originalNickname)
                .profileImgUrl("https://img.example/test.png")
                .locale("en")
                .difficulty("HARD")
                .build();
        memberRepository.insertMember(member);

        memberId = member.getMemberId();
        accessToken = jwtTokenProvider.createAccessToken(memberId);
    }

    @Test
    @DisplayName("난이도만 수정하면 닉네임·언어는 DB에서도 그대로 유지된다")
    void updatesOnlyDifficulty() throws Exception {
        mockMvc.perform(patch("/api/members/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"difficulty\":\"easy\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memberId").value(memberId))
                .andExpect(jsonPath("$.data.nickname").value(originalNickname))
                .andExpect(jsonPath("$.data.difficulty").value("EASY"))
                .andExpect(jsonPath("$.data.locale").value("en"))
                .andExpect(jsonPath("$.data.profileImage").value("https://img.example/test.png"));

        Member updated = memberRepository.findById(memberId);
        assertThat(updated.getDifficulty()).isEqualTo("EASY");
        assertThat(updated.getNickname()).isEqualTo(originalNickname);
        assertThat(updated.getLocale()).isEqualTo("en"); // 기본값 ko로 덮이지 않아야 한다
    }

    @Test
    @DisplayName("언어만 수정하면 난이도가 유지된다")
    void updatesOnlyLocale() throws Exception {
        mockMvc.perform(patch("/api/members/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"locale\":\"KO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.locale").value("ko"))
                .andExpect(jsonPath("$.data.difficulty").value("HARD"));

        Member updated = memberRepository.findById(memberId);
        assertThat(updated.getLocale()).isEqualTo("ko");
        assertThat(updated.getDifficulty()).isEqualTo("HARD");
    }

    @Test
    @DisplayName("세 필드를 한 번에 수정할 수 있다")
    void updatesAllFields() throws Exception {
        String newNickname = "새이름" + UUID.randomUUID().toString().substring(0, 6);

        mockMvc.perform(patch("/api/members/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"" + newNickname + "\",\"difficulty\":\"NORMAL\",\"locale\":\"ko\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원 정보가 수정되었습니다."))
                .andExpect(jsonPath("$.data.nickname").value(newNickname))
                .andExpect(jsonPath("$.data.difficulty").value("NORMAL"))
                .andExpect(jsonPath("$.data.locale").value("ko"));

        Member updated = memberRepository.findById(memberId);
        assertThat(updated.getNickname()).isEqualTo(newNickname);
    }

    @Test
    @DisplayName("본문이 빈 객체면 400")
    void rejectsEmptyBody() throws Exception {
        mockMvc.perform(patch("/api/members/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("허용되지 않은 난이도 값이면 400")
    void rejectsInvalidDifficulty() throws Exception {
        mockMvc.perform(patch("/api/members/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"difficulty\":\"IMPOSSIBLE\"}"))
                .andExpect(status().isBadRequest());

        assertThat(memberRepository.findById(memberId).getDifficulty()).isEqualTo("HARD");
    }

    @Test
    @DisplayName("닉네임 길이 규칙(2~12자)을 위반하면 400")
    void rejectsInvalidNickname() throws Exception {
        mockMvc.perform(patch("/api/members/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"짧\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("토큰이 없으면 401")
    void rejectsMissingToken() throws Exception {
        mockMvc.perform(patch("/api/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"difficulty\":\"EASY\"}"))
                .andExpect(status().isUnauthorized());
    }
}
