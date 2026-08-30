package com.ucamp.gyeongjuma_be.quiz;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucamp.gyeongjuma_be.auth.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 퀴즈 언어 필터와 보상 중복 방지 검증.
 *
 * 핵심: 같은 문제의 한국어판을 풀어 포인트를 받은 회원이, 언어를 바꿔 영어판을 풀어도
 *       포인트를 다시 받지 않아야 한다 (COALESCE(origin_quiz_id, quiz_id) 기준 중복 방지).
 *
 * 공유 DB이므로 롤백 트랜잭션 안에서만 데이터를 만들고 내가 만든 ID로만 단언한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("퀴즈 언어 필터 · 보상 중복 방지")
class QuizLanguageAndRewardTest {

    private static final int POINT_PER_CORRECT = 50;

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Long memberId;
    private String bearer;
    private Long placeId;
    private Long koSetId, enSetId;
    private Long koQuizId, enQuizId;
    private Long koCorrectAnswerId, enCorrectAnswerId;

    private Long lastId() {
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    /** 세트 하나 + 문항 하나(정답/오답 각 1개) 생성. originQuizId가 null이면 원본. */
    private Long[] createSet(String title, String language, Long originInfoId, Long originQuizId) {
        jdbcTemplate.update("""
                INSERT INTO place_quiz_info (place_id, title, description, difficulty, language,
                                             origin_info_id, created_at, updated_at, is_active)
                VALUES (?, ?, '설명', 'NORMAL', ?, ?, NOW(), NOW(), TRUE)
                """, placeId, title, language, originInfoId);
        Long setId = lastId();

        jdbcTemplate.update("""
                INSERT INTO quiz (question, origin_quiz_id, created_at, updated_at, is_active)
                VALUES (?, ?, NOW(), NOW(), TRUE)
                """, title + " 문항", originQuizId);
        Long quizId = lastId();

        jdbcTemplate.update("INSERT INTO place_quiz (place_quiz_info_id, quiz_id) VALUES (?, ?)", setId, quizId);
        jdbcTemplate.update("INSERT INTO quiz_answer (content, quiz_id, is_correct) VALUES ('정답', ?, TRUE)", quizId);
        Long correctAnswerId = lastId();
        jdbcTemplate.update("INSERT INTO quiz_answer (content, quiz_id, is_correct) VALUES ('오답', ?, FALSE)", quizId);

        return new Long[]{setId, quizId, correctAnswerId};
    }

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        jdbcTemplate.update("""
                INSERT INTO member (provider, provider_id, nickname, profile_img_url, locale, difficulty,
                                    `point`, distance, visit_place_cnt, created_at, updated_at, is_active)
                VALUES ('DEMO', ?, ?, 'https://i/p.png', 'ko', 'NORMAL', 0, 0, 0, NOW(), NOW(), TRUE)
                """, "demo-" + suffix, "퀴즈" + suffix.substring(0, 4));
        memberId = lastId();
        bearer = "Bearer " + jwtTokenProvider.createAccessToken(memberId);

        placeId = jdbcTemplate.queryForObject(
                "SELECT place_id FROM place WHERE is_active = TRUE ORDER BY place_id LIMIT 1", Long.class);
        // 퀴즈가 LOCKED 되지 않도록 방문 이력을 넣는다
        jdbcTemplate.update("INSERT INTO visit (member_id, place_id, created_at) VALUES (?, ?, NOW())",
                memberId, placeId);

        Long[] ko = createSet("한국어세트(테스트)", "ko", null, null);
        koSetId = ko[0]; koQuizId = ko[1]; koCorrectAnswerId = ko[2];

        Long[] en = createSet("EnglishSet(test)", "en", koSetId, koQuizId);
        enSetId = en[0]; enQuizId = en[1]; enCorrectAnswerId = en[2];
    }

    private long currentPoint() {
        return jdbcTemplate.queryForObject(
                "SELECT `point` FROM member WHERE member_id = ?", Long.class, memberId);
    }

    private int rewardRows() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM point_history WHERE member_id = ?", Integer.class, memberId);
    }

    private void setLocale(String locale) {
        jdbcTemplate.update("UPDATE member SET locale = ? WHERE member_id = ?", locale, memberId);
    }

    private void submit(Long setId, Long quizId, Long answerId) throws Exception {
        mockMvc.perform(post("/api/quizzes/{id}/submit", setId)
                        .header(HttpHeaders.AUTHORIZATION, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionId\":%d,\"selectedOptionId\":%d}".formatted(quizId, answerId)))
                .andExpect(status().isOk());
    }

    private List<Long> listedSetIds(String memberBearer) throws Exception {
        String body = mockMvc.perform(get("/api/quizzes").header(HttpHeaders.AUTHORIZATION, memberBearer))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<Long> ids = new ArrayList<>();
        for (JsonNode n : objectMapper.readTree(body).path("data").path("quizList")) {
            ids.add(n.path("placeQuizInfoId").asLong());
        }
        return ids;
    }

    /** 지정한 언어의 회원을 새로 만들고, 방문 이력까지 넣어 토큰을 돌려준다 */
    private String createVisitingMember(String locale) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        jdbcTemplate.update("""
                INSERT INTO member (provider, provider_id, nickname, profile_img_url, locale, difficulty,
                                    `point`, distance, visit_place_cnt, created_at, updated_at, is_active)
                VALUES ('DEMO', ?, ?, 'https://i/p.png', ?, 'NORMAL', 0, 0, 0, NOW(), NOW(), TRUE)
                """, "demo-" + suffix, "회원" + suffix.substring(0, 4), locale);
        Long id = lastId();
        jdbcTemplate.update("INSERT INTO visit (member_id, place_id, created_at) VALUES (?, ?, NOW())",
                id, placeId);
        return "Bearer " + jwtTokenProvider.createAccessToken(id);
    }

    @Test
    @DisplayName("한국어 회원에게는 ko 세트만, 영어 회원에게는 en 세트만 보인다")
    void filtersByLocale() throws Exception {
        // 같은 회원의 locale을 바꿔 두 번 조회하면 MyBatis 1차 캐시에 걸리므로 회원을 나눈다
        assertThat(listedSetIds(bearer))
                .contains(koSetId)
                .doesNotContain(enSetId);

        String enBearer = createVisitingMember("en");
        assertThat(listedSetIds(enBearer))
                .contains(enSetId)
                .doesNotContain(koSetId);
    }

    @Test
    @DisplayName("한국어판을 맞히면 포인트를 받는다")
    void grantsPointOnFirstCorrectAnswer() throws Exception {
        long before = currentPoint();

        submit(koSetId, koQuizId, koCorrectAnswerId);

        assertThat(currentPoint()).isEqualTo(before + POINT_PER_CORRECT);
        assertThat(rewardRows()).isEqualTo(1);
    }

    @Test
    @DisplayName("언어를 바꿔 번역본을 다시 맞혀도 포인트가 재지급되지 않는다")
    void doesNotGrantPointAgainForTranslatedQuestion() throws Exception {
        submit(koSetId, koQuizId, koCorrectAnswerId);
        long afterKorean = currentPoint();
        assertThat(rewardRows()).isEqualTo(1);

        // 회원 언어를 영어로 바꾸고 같은 문제의 영어판을 맞힌다
        setLocale("en");
        submit(enSetId, enQuizId, enCorrectAnswerId);

        assertThat(currentPoint())
                .as("번역본은 원본과 같은 중복 방지 키를 쓰므로 포인트가 늘면 안 된다")
                .isEqualTo(afterKorean);
        assertThat(rewardRows()).isEqualTo(1);
    }

    @Test
    @DisplayName("영어판을 먼저 풀어도 한국어판에서 재지급되지 않는다 (순서 무관)")
    void dedupWorksInReverseOrder() throws Exception {
        setLocale("en");
        submit(enSetId, enQuizId, enCorrectAnswerId);
        long afterEnglish = currentPoint();
        assertThat(rewardRows()).isEqualTo(1);

        setLocale("ko");
        submit(koSetId, koQuizId, koCorrectAnswerId);

        assertThat(currentPoint()).isEqualTo(afterEnglish);
        assertThat(rewardRows()).isEqualTo(1);
    }

    @Test
    @DisplayName("중복 방지 키는 원본 문항 ID로 기록된다")
    void rewardKeyUsesOriginQuizId() throws Exception {
        setLocale("en");
        submit(enSetId, enQuizId, enCorrectAnswerId);

        String description = jdbcTemplate.queryForObject(
                "SELECT description FROM point_history WHERE member_id = ?", String.class, memberId);
        assertThat(description)
                .as("영어판을 풀었어도 키는 원본 quiz_id 기준이어야 한다")
                .isEqualTo(koQuizId + "_" + memberId);
    }

    @Test
    @DisplayName("서로 다른 문제는 각각 포인트를 받는다")
    void grantsPointPerDistinctQuestion() throws Exception {
        Long[] another = createSet("다른세트(테스트)", "ko", null, null);

        submit(koSetId, koQuizId, koCorrectAnswerId);
        submit(another[0], another[1], another[2]);

        assertThat(rewardRows()).isEqualTo(2);
    }

    @Test
    @DisplayName("오답이면 포인트가 지급되지 않는다")
    void noPointForWrongAnswer() throws Exception {
        Long wrongAnswerId = jdbcTemplate.queryForObject(
                "SELECT answer_id FROM quiz_answer WHERE quiz_id = ? AND is_correct = FALSE",
                Long.class, koQuizId);
        long before = currentPoint();

        submit(koSetId, koQuizId, wrongAnswerId);

        assertThat(currentPoint()).isEqualTo(before);
        assertThat(rewardRows()).isZero();
    }
}
