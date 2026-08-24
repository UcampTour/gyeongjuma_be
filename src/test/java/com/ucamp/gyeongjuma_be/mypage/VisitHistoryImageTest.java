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
 * GET /api/mypage/visits 의 대표 이미지(imageUrl) 응답 검증.
 * 공유 DB이므로 롤백 트랜잭션 안에서 데모 회원의 방문 이력만 만들고, 그 회원 기준으로만 단언한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("GET /api/mypage/visits - 대표 이미지")
class VisitHistoryImageTest {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired JwtTokenProvider jwtTokenProvider;

    private String bearer;
    private Long placeWithImage;
    private Long placeWithoutImage;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        jdbcTemplate.update("""
                INSERT INTO member (provider, provider_id, nickname, profile_img_url, locale, difficulty,
                                    `point`, distance, visit_place_cnt, created_at, updated_at, is_active)
                VALUES ('DEMO', ?, ?, 'https://img.example.com/p.png', 'ko', 'NORMAL',
                        0, 0, 0, NOW(), NOW(), TRUE)
                """, "demo-" + suffix, "방문테스트" + suffix.substring(0, 4));
        Long memberId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        bearer = "Bearer " + jwtTokenProvider.createAccessToken(memberId);

        // 실제 데이터에서 이미지가 있는 장소 / 빈 문자열인 장소를 하나씩 고른다
        placeWithImage = jdbcTemplate.queryForObject(
                "SELECT place_id FROM place WHERE first_image <> '' AND is_active = TRUE ORDER BY place_id LIMIT 1",
                Long.class);
        List<Long> empties = jdbcTemplate.queryForList(
                "SELECT place_id FROM place WHERE first_image = '' AND is_active = TRUE ORDER BY place_id LIMIT 1",
                Long.class);
        placeWithoutImage = empties.isEmpty() ? null : empties.get(0);

        jdbcTemplate.update("INSERT INTO visit (member_id, place_id, created_at) VALUES (?, ?, NOW())",
                memberId, placeWithImage);
        if (placeWithoutImage != null) {
            jdbcTemplate.update("INSERT INTO visit (member_id, place_id, created_at) VALUES (?, ?, NOW())",
                    memberId, placeWithoutImage);
        }
    }

    @Test
    @DisplayName("이미지가 있는 장소는 URL을, 빈 문자열인 장소는 null을 반환한다")
    void returnsImageUrlOrNull() throws Exception {
        String body = mockMvc.perform(get("/api/mypage/visits").header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        JsonNode visits = new ObjectMapper().readTree(body).path("data").path("visits");
        // 내 데모 회원의 이력만 들어 있으므로 이 회원 기준 개수 단언이 안전하다
        assertThat(visits).hasSize(placeWithoutImage == null ? 1 : 2);

        JsonNode withImage = findByPlaceId(visits, placeWithImage);
        assertThat(withImage.get("imageUrl").isNull()).isFalse();
        assertThat(withImage.get("imageUrl").asText()).startsWith("http");
        assertThat(withImage.get("placeName").asText()).isNotBlank();

        if (placeWithoutImage != null) {
            // 빈 문자열이 아니라 null로 내려가야 프론트가 placeholder 분기를 태울 수 있다
            JsonNode withoutImage = findByPlaceId(visits, placeWithoutImage);
            assertThat(withoutImage.get("imageUrl").isNull()).isTrue();
            assertThat(body).doesNotContain("\"imageUrl\":\"\"");
        }
    }

    private JsonNode findByPlaceId(JsonNode visits, Long placeId) {
        for (JsonNode v : visits) {
            if (v.path("placeId").asLong() == placeId) {
                return v;
            }
        }
        throw new AssertionError("placeId " + placeId + " 가 방문 이력에 없습니다: " + visits);
    }

    @Test
    @DisplayName("토큰이 없으면 401")
    void rejectsMissingToken() throws Exception {
        mockMvc.perform(get("/api/mypage/visits"))
                .andExpect(status().isUnauthorized());
    }
}
