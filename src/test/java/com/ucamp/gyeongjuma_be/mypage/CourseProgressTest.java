package com.ucamp.gyeongjuma_be.mypage;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GET /api/mypage/courses — "코스의 장소를 모두 방문한 적이 있으면 완주" 규칙 검증.
 *
 * 사용자 예시 그대로:
 *   코스1 = 장소 A,B,C  → A,B,C 방문 → 완주
 *   코스2 = 장소 A,C,D  → D 미방문   → 미완주
 *
 * 공유 DB이므로 롤백 트랜잭션 안에서만 데이터를 만들고, 내가 만든 courseId로 필터해 단언한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("GET /api/mypage/courses - 코스 완주 판정")
class CourseProgressTest {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Long memberId;
    private String bearer;
    private Long placeA, placeB, placeC, placeD;
    private Long course1, course2;

    private Long lastId() {
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    private Long createCourse(String name, List<Long> placeIds) {
        jdbcTemplate.update("""
                INSERT INTO course (name, place_cnt, created_at, updated_at, is_active)
                VALUES (?, ?, NOW(), NOW(), TRUE)
                """, name, placeIds.size());
        Long courseId = lastId();
        for (int i = 0; i < placeIds.size(); i++) {
            jdbcTemplate.update(
                    "INSERT INTO course_place (course_id, place_id, sort_order) VALUES (?, ?, ?)",
                    courseId, placeIds.get(i), i + 1);
        }
        return courseId;
    }

    private void visit(Long placeId) {
        jdbcTemplate.update("INSERT INTO visit (member_id, place_id, created_at) VALUES (?, ?, NOW())",
                memberId, placeId);
    }

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        jdbcTemplate.update("""
                INSERT INTO member (provider, provider_id, nickname, profile_img_url, locale, difficulty,
                                    `point`, distance, visit_place_cnt, created_at, updated_at, is_active)
                VALUES ('DEMO', ?, ?, 'https://i/p.png', 'ko', 'NORMAL', 0, 0, 0, NOW(), NOW(), TRUE)
                """, "demo-" + suffix, "코스" + suffix.substring(0, 4));
        memberId = lastId();
        bearer = "Bearer " + jwtTokenProvider.createAccessToken(memberId);

        List<Long> places = jdbcTemplate.queryForList(
                "SELECT place_id FROM place WHERE is_active = TRUE ORDER BY place_id LIMIT 4", Long.class);
        placeA = places.get(0);
        placeB = places.get(1);
        placeC = places.get(2);
        placeD = places.get(3);

        course1 = createCourse("코스1(테스트-" + suffix + ")", List.of(placeA, placeB, placeC));
        course2 = createCourse("코스2(테스트-" + suffix + ")", List.of(placeA, placeC, placeD));
    }

    private JsonNode courses() throws Exception {
        String body = mockMvc.perform(get("/api/mypage/courses").header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(body).path("data");
    }

    private JsonNode find(JsonNode data, Long courseId) {
        for (JsonNode c : data.path("courses")) {
            if (c.path("courseId").asLong() == courseId) {
                return c;
            }
        }
        throw new AssertionError("courseId " + courseId + " 없음: " + data);
    }

    @Test
    @DisplayName("A,B,C 방문 시 코스1은 완주, D가 빠진 코스2는 미완주")
    void completesCourse1ButNotCourse2() throws Exception {
        visit(placeA);
        visit(placeB);
        visit(placeC);

        JsonNode data = courses();

        JsonNode c1 = find(data, course1);
        assertThat(c1.path("placeCnt").asInt()).isEqualTo(3);
        assertThat(c1.path("visitedCnt").asInt()).isEqualTo(3);
        assertThat(c1.path("progressRate").asDouble()).isEqualTo(100.00);
        assertThat(c1.path("isCompleted").asBoolean()).isTrue();

        JsonNode c2 = find(data, course2);
        assertThat(c2.path("placeCnt").asInt()).isEqualTo(3);
        assertThat(c2.path("visitedCnt").asInt()).isEqualTo(2);   // A, C만
        assertThat(c2.path("progressRate").asDouble()).isEqualTo(66.67);
        assertThat(c2.path("isCompleted").asBoolean()).isFalse();
    }

    @Test
    @DisplayName("한 곳도 안 갔으면 진행률 0이고 목록에는 나온다")
    void showsUntouchedCourses() throws Exception {
        JsonNode data = courses();

        JsonNode c1 = find(data, course1);
        assertThat(c1.path("visitedCnt").asInt()).isZero();
        assertThat(c1.path("progressRate").asDouble()).isZero();
        assertThat(c1.path("isCompleted").asBoolean()).isFalse();
    }

    @Test
    @DisplayName("같은 장소를 두 번 방문해도 중복 집계되지 않는다")
    void countsDistinctPlacesOnly() throws Exception {
        visit(placeA);
        visit(placeA);
        visit(placeA);

        JsonNode c1 = find(courses(), course1);
        assertThat(c1.path("visitedCnt").asInt()).isEqualTo(1);
        assertThat(c1.path("progressRate").asDouble()).isEqualTo(33.33);
    }

    @Test
    @DisplayName("코스 등록 전에 한 방문도 소급 적용된다")
    void countsVisitsMadeBeforeCourseExisted() throws Exception {
        // setUp에서 코스를 먼저 만들었으므로, 여기서 만드는 코스는 '방문 이후' 생성된 코스다
        visit(placeA);
        visit(placeB);
        Long laterCourse = createCourse("나중코스(테스트)", List.of(placeA, placeB));

        JsonNode c = find(courses(), laterCourse);
        assertThat(c.path("isCompleted").asBoolean()).isTrue();
        assertThat(c.path("progressRate").asDouble()).isEqualTo(100.00);
    }

    @Test
    @DisplayName("삭제된 코스는 목록에서 빠진다")
    void hidesInactiveCourse() throws Exception {
        jdbcTemplate.update("UPDATE course SET is_active = FALSE WHERE course_id = ?", course2);

        JsonNode data = courses();
        assertThat(find(data, course1)).isNotNull();
        for (JsonNode c : data.path("courses")) {
            assertThat(c.path("courseId").asLong()).isNotEqualTo(course2);
        }
    }

    @Test
    @DisplayName("비활성 장소는 분모에서 빠져 완주가 막히지 않는다")
    void excludesInactivePlaceFromDenominator() throws Exception {
        // 코스1의 C를 비활성화 → 분모가 3에서 2로 줄고, A·B만 방문해도 완주
        jdbcTemplate.update("UPDATE place SET is_active = FALSE WHERE place_id = ?", placeC);
        visit(placeA);
        visit(placeB);

        JsonNode c1 = find(courses(), course1);
        assertThat(c1.path("placeCnt").asInt()).isEqualTo(2);
        assertThat(c1.path("isCompleted").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("completedCnt는 완주한 코스 수를 센다")
    void countsCompletedCourses() throws Exception {
        visit(placeA);
        visit(placeB);
        visit(placeC);

        JsonNode data = courses();
        // 내가 만든 코스 2개 중 1개만 완주 (전체 카운트가 아니라 내 코스 기준으로 확인)
        assertThat(find(data, course1).path("isCompleted").asBoolean()).isTrue();
        assertThat(find(data, course2).path("isCompleted").asBoolean()).isFalse();
        assertThat(data.path("completedCnt").asInt()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("토큰이 없으면 401")
    void rejectsMissingToken() throws Exception {
        mockMvc.perform(get("/api/mypage/courses")).andExpect(status().isUnauthorized());
    }
}
