package com.ucamp.gyeongjuma_be.admin.controller;

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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 퀴즈 번역본 등록 통합 테스트.
 * 롤백 트랜잭션 안에서 원본 세트를 직접 만들고, 그 세트 ID 기준으로만 단언한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("/api/admin/quizzes 번역본 API 통합 테스트")
class AdminQuizTranslationTest {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String adminBearer;
    private Long originSetId;
    private List<Long> originQuizIds;

    private Long lastId() {
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        jdbcTemplate.update("""
                INSERT INTO member (provider, provider_id, nickname, profile_img_url, locale, difficulty,
                                    role, `point`, distance, visit_place_cnt, created_at, updated_at, is_active)
                VALUES ('DEMO', ?, ?, 'https://i/p.png', 'ko', 'NORMAL', 'ADMIN', 0,0,0, NOW(), NOW(), TRUE)
                """, "demo-" + suffix, "adm" + suffix.substring(0, 5));
        adminBearer = "Bearer " + jwtTokenProvider.createAccessToken(lastId());

        Long placeId = jdbcTemplate.queryForObject(
                "SELECT place_id FROM place WHERE is_active = TRUE ORDER BY place_id LIMIT 1", Long.class);

        // 한국어 원본 세트 (문항 2개)
        jdbcTemplate.update("""
                INSERT INTO place_quiz_info (place_id, title, description, difficulty, language,
                                             origin_info_id, created_at, updated_at, is_active)
                VALUES (?, '원본세트(테스트)', '설명', 'NORMAL', 'ko', NULL, NOW(), NOW(), TRUE)
                """, placeId);
        originSetId = lastId();

        originQuizIds = new java.util.ArrayList<>();
        for (String q : new String[]{"첨성대는 몇 단인가요?", "창문은 어느 방향인가요?"}) {
            jdbcTemplate.update("""
                    INSERT INTO quiz (question, origin_quiz_id, created_at, updated_at, is_active)
                    VALUES (?, NULL, NOW(), NOW(), TRUE)
                    """, q);
            Long quizId = lastId();
            originQuizIds.add(quizId);
            jdbcTemplate.update(
                    "INSERT INTO place_quiz (place_quiz_info_id, quiz_id) VALUES (?, ?)", originSetId, quizId);
            jdbcTemplate.update(
                    "INSERT INTO quiz_answer (content, quiz_id, is_correct) VALUES ('보기1', ?, TRUE)", quizId);
            jdbcTemplate.update(
                    "INSERT INTO quiz_answer (content, quiz_id, is_correct) VALUES ('보기2', ?, FALSE)", quizId);
        }
    }

    private String englishBody() {
        return """
                {"language":"EN","title":"Cheomseongdae Quiz","description":"desc",
                 "questions":[
                   {"originQuizId":%d,"question":"How many tiers?",
                    "answers":[{"content":"27","isCorrect":true},{"content":"24","isCorrect":false}]},
                   {"originQuizId":%d,"question":"Which direction?",
                    "answers":[{"content":"South","isCorrect":true},{"content":"North","isCorrect":false}]}]}
                """.formatted(originQuizIds.get(0), originQuizIds.get(1));
    }

    @Test
    @DisplayName("번역본을 등록하면 원본 연결이 채워지고 언어는 소문자로 정규화된다")
    void createsTranslation() throws Exception {
        String body = mockMvc.perform(post("/api/admin/quizzes/{id}/translations", originSetId)
                        .header(HttpHeaders.AUTHORIZATION, adminBearer)
                        .contentType(MediaType.APPLICATION_JSON).content(englishBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("번역본을 등록했습니다."))
                .andExpect(jsonPath("$.data.quizSet.language").value("en"))
                .andExpect(jsonPath("$.data.quizSet.originInfoId").value(originSetId))
                .andExpect(jsonPath("$.data.questionCnt").value(2))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        JsonNode data = objectMapper.readTree(body).path("data");
        Long translatedSetId = data.path("quizSet").path("placeQuizInfoId").asLong();

        // 번역 문항이 원본 문항을 가리켜야 포인트 중복이 막힌다
        List<Long> origins = jdbcTemplate.queryForList("""
                SELECT q.origin_quiz_id FROM place_quiz pq
                  JOIN quiz q ON q.quiz_id = pq.quiz_id
                 WHERE pq.place_quiz_info_id = ? ORDER BY q.quiz_id
                """, Long.class, translatedSetId);
        assertThat(origins).containsExactlyElementsOf(originQuizIds);

        // 원본의 장소·난이도를 그대로 물려받는다
        assertThat(data.path("quizSet").path("difficulty").asText()).isEqualTo("NORMAL");
    }

    @Test
    @DisplayName("등록된 번역본은 중복 방지 키가 원본과 같아진다")
    void translationSharesDedupKeyWithOrigin() throws Exception {
        String body = mockMvc.perform(post("/api/admin/quizzes/{id}/translations", originSetId)
                        .header(HttpHeaders.AUTHORIZATION, adminBearer)
                        .contentType(MediaType.APPLICATION_JSON).content(englishBody()))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        Long translatedSetId = objectMapper.readTree(body)
                .path("data").path("quizSet").path("placeQuizInfoId").asLong();

        // 보상 쿼리가 쓸 COALESCE(origin_quiz_id, quiz_id) 가 원본 quiz_id 와 일치해야 한다
        List<Long> dedupKeys = jdbcTemplate.queryForList("""
                SELECT COALESCE(q.origin_quiz_id, q.quiz_id) FROM place_quiz pq
                  JOIN quiz q ON q.quiz_id = pq.quiz_id
                 WHERE pq.place_quiz_info_id = ? ORDER BY q.quiz_id
                """, Long.class, translatedSetId);
        assertThat(dedupKeys).containsExactlyElementsOf(originQuizIds);
    }

    @Test
    @DisplayName("같은 언어 번역본을 두 번 등록하면 409 Q003")
    void rejectsDuplicateLanguage() throws Exception {
        mockMvc.perform(post("/api/admin/quizzes/{id}/translations", originSetId)
                .header(HttpHeaders.AUTHORIZATION, adminBearer)
                .contentType(MediaType.APPLICATION_JSON).content(englishBody()));

        mockMvc.perform(post("/api/admin/quizzes/{id}/translations", originSetId)
                        .header(HttpHeaders.AUTHORIZATION, adminBearer)
                        .contentType(MediaType.APPLICATION_JSON).content(englishBody()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("Q003"));
    }

    @Test
    @DisplayName("원본과 같은 언어(ko)로는 번역본을 만들 수 없다")
    void rejectsSameLanguageAsOrigin() throws Exception {
        String koBody = englishBody().replace("\"language\":\"EN\"", "\"language\":\"ko\"");
        mockMvc.perform(post("/api/admin/quizzes/{id}/translations", originSetId)
                        .header(HttpHeaders.AUTHORIZATION, adminBearer)
                        .contentType(MediaType.APPLICATION_JSON).content(koBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("Q004"));
    }

    @Test
    @DisplayName("원본 문항을 빠뜨리면 400 Q004")
    void rejectsMissingOriginQuestion() throws Exception {
        String partial = """
                {"language":"ja","title":"クイズ","questions":[
                  {"originQuizId":%d,"question":"何段？",
                   "answers":[{"content":"27","isCorrect":true},{"content":"24","isCorrect":false}]}]}
                """.formatted(originQuizIds.get(0));

        mockMvc.perform(post("/api/admin/quizzes/{id}/translations", originSetId)
                        .header(HttpHeaders.AUTHORIZATION, adminBearer)
                        .contentType(MediaType.APPLICATION_JSON).content(partial))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("Q004"));
    }

    @Test
    @DisplayName("원본 세트에 없는 문항을 지정하면 400 Q004")
    void rejectsForeignQuestion() throws Exception {
        String foreign = englishBody().replace(
                "\"originQuizId\":" + originQuizIds.get(1), "\"originQuizId\":99999999");

        mockMvc.perform(post("/api/admin/quizzes/{id}/translations", originSetId)
                        .header(HttpHeaders.AUTHORIZATION, adminBearer)
                        .contentType(MediaType.APPLICATION_JSON).content(foreign))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("Q004"));
    }

    @Test
    @DisplayName("정답이 2개인 문항이 있으면 400 Q002")
    void rejectsTwoCorrectAnswers() throws Exception {
        String bad = englishBody().replace("{\"content\":\"24\",\"isCorrect\":false}",
                "{\"content\":\"24\",\"isCorrect\":true}");

        mockMvc.perform(post("/api/admin/quizzes/{id}/translations", originSetId)
                        .header(HttpHeaders.AUTHORIZATION, adminBearer)
                        .contentType(MediaType.APPLICATION_JSON).content(bad))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("Q002"));
    }

    @Test
    @DisplayName("번역본의 번역본은 만들 수 없다")
    void rejectsTranslationOfTranslation() throws Exception {
        String body = mockMvc.perform(post("/api/admin/quizzes/{id}/translations", originSetId)
                        .header(HttpHeaders.AUTHORIZATION, adminBearer)
                        .contentType(MediaType.APPLICATION_JSON).content(englishBody()))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        Long translatedSetId = objectMapper.readTree(body)
                .path("data").path("quizSet").path("placeQuizInfoId").asLong();

        mockMvc.perform(post("/api/admin/quizzes/{id}/translations", translatedSetId)
                        .header(HttpHeaders.AUTHORIZATION, adminBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(englishBody().replace("\"language\":\"EN\"", "\"language\":\"ja\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("Q004"));
    }

    @Test
    @DisplayName("번역본 목록과 language 필터가 동작한다")
    void listsTranslationsAndFiltersByLanguage() throws Exception {
        mockMvc.perform(post("/api/admin/quizzes/{id}/translations", originSetId)
                .header(HttpHeaders.AUTHORIZATION, adminBearer)
                .contentType(MediaType.APPLICATION_JSON).content(englishBody()));

        mockMvc.perform(get("/api/admin/quizzes/{id}/translations", originSetId)
                        .header(HttpHeaders.AUTHORIZATION, adminBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].language").value("en"))
                .andExpect(jsonPath("$.data[0].originInfoId").value(originSetId));

        // language=en 으로 목록을 거르면 내 원본(ko)은 안 잡혀야 한다
        String list = mockMvc.perform(get("/api/admin/quizzes")
                        .param("language", "en").param("size", "100")
                        .header(HttpHeaders.AUTHORIZATION, adminBearer))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        for (JsonNode set : objectMapper.readTree(list).path("data").path("quizSets")) {
            assertThat(set.path("language").asText()).isEqualTo("en");
            assertThat(set.path("placeQuizInfoId").asLong()).isNotEqualTo(originSetId);
        }
    }

    @Test
    @DisplayName("신규 등록은 language를 생략하면 ko로 저장된다")
    void createDefaultsToKorean() throws Exception {
        Long placeId = jdbcTemplate.queryForObject(
                "SELECT place_id FROM place WHERE is_active = TRUE ORDER BY place_id LIMIT 1", Long.class);
        String body = """
                {"placeId":%d,"title":"기본언어(테스트)","difficulty":"EASY","questions":[
                  {"question":"문항","answers":[{"content":"a","isCorrect":true},{"content":"b","isCorrect":false}]}]}
                """.formatted(placeId);

        mockMvc.perform(post("/api/admin/quizzes")
                        .header(HttpHeaders.AUTHORIZATION, adminBearer)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quizSet.language").value("ko"))
                .andExpect(jsonPath("$.data.quizSet.originInfoId").doesNotExist());
    }

    @Test
    @DisplayName("일반 회원 토큰이면 403")
    void rejectsNonAdmin() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        jdbcTemplate.update("""
                INSERT INTO member (provider, provider_id, nickname, profile_img_url, locale, difficulty,
                                    role, `point`, distance, visit_place_cnt, created_at, updated_at, is_active)
                VALUES ('DEMO', ?, ?, 'https://i/p.png', 'ko', 'NORMAL', 'USER', 0,0,0, NOW(), NOW(), TRUE)
                """, "demo-" + suffix, "usr" + suffix.substring(0, 5));
        String userBearer = "Bearer " + jwtTokenProvider.createAccessToken(lastId());

        mockMvc.perform(post("/api/admin/quizzes/{id}/translations", originSetId)
                        .header(HttpHeaders.AUTHORIZATION, userBearer)
                        .contentType(MediaType.APPLICATION_JSON).content(englishBody()))
                .andExpect(status().isForbidden());
    }
}
