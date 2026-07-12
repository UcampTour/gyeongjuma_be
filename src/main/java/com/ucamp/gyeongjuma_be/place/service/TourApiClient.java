package com.ucamp.gyeongjuma_be.place.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucamp.gyeongjuma_be.place.domain.Place;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class TourApiClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${tour.api.service-key:}")
    private String serviceKey;

    public String getPlaceXml() {
        if (serviceKey == null || serviceKey.isBlank()) {
            throw new IllegalStateException("service-key 설정이 필요합니다.");
        }

        String url = "https://apis.data.go.kr/B551011/KorService2/areaBasedList2"
                + "?serviceKey=" + encodedServiceKey()
                + "&MobileOS=WEB"
                + "&MobileApp=Gyeongjuma"
                + "&_type=xml"
                + "&numOfRows=1000"
                + "&pageNo=1"
                + "&contentTypeId=12"
                + "&lDongRegnCd=47"
                + "&lDongSignguCd=130"
                + "&lclsSystm1=HS";

        byte[] response = restTemplate.getForObject(URI.create(url), byte[].class);

        if (response == null) {
            return null;
        }

        return new String(response, StandardCharsets.UTF_8);
    }

    public List<Place> getPlaceList() {
        String xml = getPlaceXml();

        if (xml == null || xml.isBlank()) {
            return List.of();
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            Document document = factory.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)));
            NodeList items = document.getElementsByTagName("item");
            List<Place> places = new ArrayList<>();

            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                Long placeId = parseLong(text(item, "contentid"));

                if (placeId == null) {
                    continue;
                }

                places.add(Place.builder()
                        .placeId(placeId)
                        .placeName(text(item, "title"))
                        .add1(text(item, "addr1"))
                        .add2(text(item, "addr2"))
                        .tel(text(item, "tel"))
                        .contentTypeId(parseLong(text(item, "contenttypeid")))
                        .mapX(parseDouble(text(item, "mapx")))
                        .mapY(parseDouble(text(item, "mapy")))
                        .firstImage(text(item, "firstimage"))
                        .lclsSystm1(text(item, "lclsSystm1"))
                        .lclsSystm2(text(item, "lclsSystm2"))
                        .lclsSystm3(text(item, "lclsSystm3"))
                        .radiusMeters(text(item, "radius"))
                        .build());
            }

            return places;
        } catch (Exception e) {
            throw new IllegalStateException("관광공사 장소 XML 파싱에 실패했습니다.", e);
        }
    }

    public Place getPlaceDetail(Long contentId) {
        if (serviceKey == null || serviceKey.isBlank()) {
            throw new IllegalStateException("service-key ?ㅼ젙???꾩슂?⑸땲??");
        }

        String url = "https://apis.data.go.kr/B551011/KorService2/detailIntro2"
                + "?serviceKey=" + encodedServiceKey()
                + "&MobileOS=WEB"
                + "&MobileApp=Gyeongjuma"
                + "&_type=xml"
                + "&contentId=" + contentId
                + "&contentTypeId=12";

        byte[] response = restTemplate.getForObject(URI.create(url), byte[].class);

        if (response == null) {
            return null;
        }

        String xml = new String(response, StandardCharsets.UTF_8);

        if (xml.isBlank()) {
            return null;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            Document document = factory.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)));
            NodeList items = document.getElementsByTagName("item");

            if (items.getLength() == 0) {
                return null;
            }

            Element item = (Element) items.item(0);

            return Place.builder()
                    .placeId(contentId)
                    .tel(text(item, "infocenter"))
                    .parking(text(item, "parking"))
                    .usetime(text(item, "usetime"))
                    .restdate(text(item, "restdate"))
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("愿愿묎났???곸꽭 XML ?뚯떛???ㅽ뙣?덉뒿?덌떎.", e);
        }
    }

    public String getPlaceOverview(Long contentId) {
        if (serviceKey == null || serviceKey.isBlank()) {
            throw new IllegalStateException("service-key 설정이 필요합니다.");
        }

        String url = "https://apis.data.go.kr/B551011/KorService2/detailCommon2"
                + "?serviceKey=" + encodedServiceKey()
                + "&MobileOS=ETC"
                + "&MobileApp=AppTest"
                + "&_type=json"
                + "&contentId=" + contentId
                + "&numOfRows=10"
                + "&pageNo=1";

        byte[] response = restTemplate.getForObject(URI.create(url), byte[].class);

        if (response == null) {
            return null;
        }

        try {
            JsonNode root = objectMapper.readTree(new String(response, StandardCharsets.UTF_8));
            JsonNode items = root.path("response")
                    .path("body")
                    .path("items")
                    .path("item");
            JsonNode item = items.isArray() ? items.path(0) : items;
            String overview = item.path("overview").asText(null);

            return overview == null || overview.isBlank() ? null : overview.trim();
        } catch (Exception e) {
            throw new IllegalStateException("관광지 설명 JSON 파싱에 실패했습니다.", e);
        }
    }

    private String encodedServiceKey() {
        String trimmedServiceKey = serviceKey.trim();

        if (trimmedServiceKey.contains("%")) {
            return trimmedServiceKey;
        }

        return URLEncoder.encode(trimmedServiceKey, StandardCharsets.UTF_8);
    }

    private String text(Element element, String tagName) {
        NodeList nodes = element.getElementsByTagName(tagName);

        if (nodes.getLength() == 0) {
            return null;
        }

        String value = nodes.item(0).getTextContent();
        return value == null || value.isBlank() ? null : value.trim();
    }

    private Long parseLong(String value) {
        if (value == null) {
            return null;
        }

        return Long.parseLong(value);
    }

    private Double parseDouble(String value) {
        if (value == null) {
            return null;
        }

        return Double.parseDouble(value);
    }
}
