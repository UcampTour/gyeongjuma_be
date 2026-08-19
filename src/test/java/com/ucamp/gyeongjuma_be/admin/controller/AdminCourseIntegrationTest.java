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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * /api/admin/courses 관리자 코스 API 통합 테스트.
 * 공유 DB이므로 롤백 트랜잭션 안에서 관리자/일반 회원을 만들고, 등록한 코스 ID 기준으로만 단언한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("/api/admin/courses 관리자 코스 API 통합 테스트")
class AdminCourseIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String adminBearer;
    private String userBearer;
    private List<Long> placeIds;

    private Long insertMember(String role) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        jdbcTemplate.update("""
                INSERT INTO member (provider, provider_id, nickname, profile_img_url, locale, difficulty,
                                    role, `point`, distance, visit_place_cnt, created_at, updated_at, is_active)
                VALUES ('DEMO', ?, ?, 'https://img.example.com/p.png', 'ko', 'NORMAL',
                        ?, 0, 0, 0, NOW(), NOW(), TRUE)
                """, "demo-" + suffix, role.toLowerCase() + suffix.substring(0, 4), role);
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    @BeforeEach
    void setUp() {
        adminBearer = "Bearer " + jwtTokenProvider.createAccessToken(insertMember("ADMIN"));
        userBearer = "Bearer " + jwtTokenProvider.createAccessToken(insertMember("USER"));
        placeIds = jdbcTemplate.queryForList(
                "SELECT place_id FROM place WHERE is_active = TRUE ORDER BY place_id LIMIT 3", Long.class);
    }

    private String body(List<Long> ids) {
        return """
                {"name":"경주 역사 탐방(테스트)","description":"불국사와 석굴암을 잇는 반나절 코스",
                 "thumbnailUrl":"https://img.example.com/course.png","placeIds":[%s]}
                """.formatted(ids.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse(""));
    }

    @Test
    @DisplayName("코스와 장소를 함께 등록하고 sort_order가 보낸 순서대로 부여된다")
    void createsCourseWithOrderedPlaces() throws Exception {
        String response = mockMvc.perform(post("/api/admin/courses")
                        .header(HttpHeaders.AUTHORIZATION, adminBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(placeIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("코스를 등록했습니다."))
                .andExpect(jsonPath("$.data.name").value("경주 역사 탐방(테스트)"))
                .andExpect(jsonPath("$.data.placeCnt").value(placeIds.size()))
                .andExpect(jsonPath("$.data.isActive").value(true))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        JsonNode data = objectMapper.readTree(response).path("data");
        Long courseId = data.path("courseId").asLong();
        JsonNode places = data.path("places");

        assertThat(places).hasSize(placeIds.size());
        for (int i = 0; i < placeIds.size(); i++) {
            assertThat(places.get(i).path("placeId").asLong()).isEqualTo(placeIds.get(i));
            assertThat(places.get(i).path("sortOrder").asInt()).isEqualTo(i + 1);
            assertThat(places.get(i).path("placeName").asText()).isNotBlank();
        }

        // DB에도 내가 등록한 코스 기준으로만 확인 (전체 카운트 단언 금지)
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM course_place WHERE course_id = ?", Integer.class, courseId))
                .isEqualTo(placeIds.size());
        assertThat(jdbcTemplate.queryForObject(
                "SELECT place_cnt FROM course WHERE course_id = ?", Long.class, courseId))
                .isEqualTo(placeIds.size());
    }

    @Test
    @DisplayName("역순으로 보내면 sort_order도 역순으로 저장된다")
    void respectsGivenOrder() throws Exception {
        List<Long> reversed = new java.util.ArrayList<>(placeIds);
        java.util.Collections.reverse(reversed);

        String response = mockMvc.perform(post("/api/admin/courses")
                        .header(HttpHeaders.AUTHORIZATION, adminBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(reversed)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        JsonNode places = objectMapper.readTree(response).path("data").path("places");
        for (int i = 0; i < reversed.size(); i++) {
            assertThat(places.get(i).path("placeId").asLong()).isEqualTo(reversed.get(i));
        }
    }

    @Test
    @DisplayName("같은 장소를 중복해서 담으면 400 CO002")
    void rejectsDuplicatePlace() throws Exception {
        mockMvc.perform(post("/api/admin/courses")
                        .header(HttpHeaders.AUTHORIZATION, adminBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(List.of(placeIds.get(0), placeIds.get(0)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CO002"));
    }

    @Test
    @DisplayName("존재하지 않는 장소가 섞이면 404 P001")
    void rejectsUnknownPlace() throws Exception {
        mockMvc.perform(post("/api/admin/courses")
                        .header(HttpHeaders.AUTHORIZATION, adminBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(List.of(placeIds.get(0), 99999999L))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("P001"));
    }

    @Test
    @DisplayName("장소 목록이 비어 있으면 400")
    void rejectsEmptyPlaceIds() throws Exception {
        mockMvc.perform(post("/api/admin/courses")
                        .header(HttpHeaders.AUTHORIZATION, adminBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"이름만\",\"placeIds\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이름이 없으면 400")
    void rejectsBlankName() throws Exception {
        mockMvc.perform(post("/api/admin/courses")
                        .header(HttpHeaders.AUTHORIZATION, adminBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  \",\"placeIds\":[" + placeIds.get(0) + "]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("일반 회원 토큰이면 403")
    void rejectsNonAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/courses")
                        .header(HttpHeaders.AUTHORIZATION, userBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(placeIds)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("A006"));
    }

    @Test
    @DisplayName("목록·상세·삭제·복구가 등록한 코스에 대해 동작한다")
    void listDetailDeleteRestore() throws Exception {
        // 등록
        String created = mockMvc.perform(post("/api/admin/courses")
                        .header(HttpHeaders.AUTHORIZATION, adminBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(placeIds)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        Long courseId = objectMapper.readTree(created).path("data").path("courseId").asLong();

        // 상세: 장소가 순서대로 딸려온다
        mockMvc.perform(get("/api/admin/courses/{id}", courseId)
                        .header(HttpHeaders.AUTHORIZATION, adminBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.courseId").value(courseId))
                .andExpect(jsonPath("$.data.places.length()").value(placeIds.size()))
                .andExpect(jsonPath("$.data.places[0].sortOrder").value(1));

        // 목록: 이름으로 검색하면 내가 만든 코스가 잡힌다 (전체 카운트 단언은 하지 않는다)
        String list = mockMvc.perform(get("/api/admin/courses")
                        .param("keyword", "경주 역사 탐방(테스트)")
                        .header(HttpHeaders.AUTHORIZATION, adminBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(containsCourse(objectMapper.readTree(list).path("data").path("courses"), courseId)).isTrue();

        // 삭제 후에는 isActive=false
        mockMvc.perform(delete("/api/admin/courses/{id}", courseId)
                        .header(HttpHeaders.AUTHORIZATION, adminBearer))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/admin/courses/{id}", courseId)
                        .header(HttpHeaders.AUTHORIZATION, adminBearer))
                .andExpect(jsonPath("$.data.isActive").value(false));

        // 이미 삭제된 코스를 또 삭제하면 400
        mockMvc.perform(delete("/api/admin/courses/{id}", courseId)
                        .header(HttpHeaders.AUTHORIZATION, adminBearer))
                .andExpect(status().isBadRequest());

        // 복구
        mockMvc.perform(patch("/api/admin/courses/{id}/restore", courseId)
                        .header(HttpHeaders.AUTHORIZATION, adminBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isActive").value(true))
                .andExpect(jsonPath("$.data.deletedAt").doesNotExist());
    }

    @Test
    @DisplayName("isActive=false로 필터하면 삭제된 코스만 나온다")
    void filtersByIsActive() throws Exception {
        String created = mockMvc.perform(post("/api/admin/courses")
                        .header(HttpHeaders.AUTHORIZATION, adminBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(placeIds)))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        Long courseId = objectMapper.readTree(created).path("data").path("courseId").asLong();

        String activeList = mockMvc.perform(get("/api/admin/courses")
                        .param("isActive", "false")
                        .header(HttpHeaders.AUTHORIZATION, adminBearer))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(containsCourse(objectMapper.readTree(activeList).path("data").path("courses"), courseId))
                .isFalse();

        mockMvc.perform(delete("/api/admin/courses/{id}", courseId)
                .header(HttpHeaders.AUTHORIZATION, adminBearer));

        String deletedList = mockMvc.perform(get("/api/admin/courses")
                        .param("isActive", "false")
                        .header(HttpHeaders.AUTHORIZATION, adminBearer))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(containsCourse(objectMapper.readTree(deletedList).path("data").path("courses"), courseId))
                .isTrue();
    }

    @Test
    @DisplayName("없는 코스를 조회·삭제·복구하면 404 CO001")
    void unknownCourseReturns404() throws Exception {
        mockMvc.perform(get("/api/admin/courses/99999999")
                        .header(HttpHeaders.AUTHORIZATION, adminBearer))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CO001"));
        mockMvc.perform(delete("/api/admin/courses/99999999")
                        .header(HttpHeaders.AUTHORIZATION, adminBearer))
                .andExpect(status().isNotFound());
        mockMvc.perform(patch("/api/admin/courses/99999999/restore")
                        .header(HttpHeaders.AUTHORIZATION, adminBearer))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("목록·상세도 일반 회원이면 403")
    void readEndpointsRejectNonAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/courses").header(HttpHeaders.AUTHORIZATION, userBearer))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/courses/1").header(HttpHeaders.AUTHORIZATION, userBearer))
                .andExpect(status().isForbidden());
    }

    private boolean containsCourse(JsonNode courses, Long courseId) {
        for (JsonNode c : courses) {
            if (c.path("courseId").asLong() == courseId) {
                return true;
            }
        }
        return false;
    }

    @Test
    @DisplayName("토큰이 없으면 401")
    void rejectsMissingToken() throws Exception {
        mockMvc.perform(post("/api/admin/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(placeIds)))
                .andExpect(status().isUnauthorized());
    }
}
