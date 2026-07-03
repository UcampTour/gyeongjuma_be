package com.ucamp.gyeongjuma_be.congestion.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucamp.gyeongjuma_be.congestion.domain.Congestion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class CongestionApiClient {

    private static final DateTimeFormatter BASE_YMD_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${tour.api.service-key:}")
    private String serviceKey;

    public List<Congestion> getCongestionList(Long placeId, String areaCd, String signguCd,
                                              String tAtsNm, int pageNo, int numOfRows) {
        if (serviceKey == null || serviceKey.isBlank()) {
            throw new IllegalStateException("tour.api.service-key 설정이 필요합니다.");
        }

        String json = getCongestionJson(areaCd, signguCd, tAtsNm, pageNo, numOfRows);

        if (json == null || json.isBlank()) {
            return List.of();
        }

        try {
            JsonNode itemNode = objectMapper.readTree(json)
                    .path("response")
                    .path("body")
                    .path("items")
                    .path("item");

            if (itemNode.isMissingNode() || itemNode.isNull()) {
                return List.of();
            }

            List<Congestion> congestions = new ArrayList<>();

            if (itemNode.isArray()) {
                for (JsonNode item : itemNode) {
                    addCongestion(congestions, item, placeId);
                }
                return congestions;
            }

            addCongestion(congestions, itemNode, placeId);
            return congestions;
        } catch (Exception e) {
            throw new IllegalStateException("관광지 혼잡도 JSON 파싱에 실패했습니다.", e);
        }
    }

    private String getCongestionJson(String areaCd, String signguCd, String tAtsNm,
                                     int pageNo, int numOfRows) {
        String url = "https://apis.data.go.kr/B551011/TatsCnctrRateService/tatsCnctrRatedList"
                + "?serviceKey=" + serviceKey
                + "&pageNo=" + pageNo
                + "&numOfRows=" + numOfRows
                + "&MobileOS=ETC"
                + "&MobileApp=Gyeongjuma"
                + "&areaCd=" + areaCd
                + "&signguCd=" + signguCd
                + "&tAtsNm=" + URLEncoder.encode(tAtsNm, StandardCharsets.UTF_8)
                + "&_type=json";

        byte[] response = restTemplate.getForObject(URI.create(url), byte[].class);

        if (response == null) {
            return null;
        }

        return new String(response, StandardCharsets.UTF_8);
    }

    private void addCongestion(List<Congestion> congestions, JsonNode item, Long placeId) {
        String baseYmd = text(item, "baseYmd");
        String cnctrRate = text(item, "cnctrRate");

        if (baseYmd == null || cnctrRate == null) {
            return;
        }

        congestions.add(Congestion.builder()
                .placeId(placeId)
                .logDate(LocalDate.parse(baseYmd, BASE_YMD_FORMATTER))
                .score(cnctrRate)
                .build());
    }

    private String text(JsonNode node, String fieldName) {
        JsonNode field = node.path(fieldName);

        if (field.isMissingNode() || field.isNull()) {
            return null;
        }

        String value = field.asText();
        return value == null || value.isBlank() ? null : value.trim();
    }
}
