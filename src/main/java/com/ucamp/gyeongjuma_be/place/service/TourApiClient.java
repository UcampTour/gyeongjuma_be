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

    public String getPlaceXml(TourApiLocale locale) {
        if (serviceKey == null || serviceKey.isBlank()) {
            throw new IllegalStateException("service-key 설정이 필요합니다.");
        }

        String url = "https://apis.data.go.kr/B551011/" + locale.serviceName() + "/areaBasedList2"
                + "?serviceKey=" + encodedServiceKey()
                + "&MobileOS=WEB"
                + "&MobileApp=Gyeongjuma"
                + "&_type=xml"
                + "&numOfRows=1000"
                + "&pageNo=1"
                + "&contentTypeId=" + locale.contentTypeId()
                + "&lDongRegnCd=47"
                + "&lDongSignguCd=130"
                + "&lclsSystm1=HS";

        byte[] response = restTemplate.getForObject(URI.create(url), byte[].class);

        if (response == null) {
            return null;
        }

        return new String(response, StandardCharsets.UTF_8);
    }

    public List<Place> getPlaceList(TourApiLocale locale) {
        String xml = getPlaceXml(locale);

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
                        .apiPlaceId(placeId)
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
                        .language(locale.contentLanguage())
                        .build());
            }

            return places;
        } catch (Exception e) {
            throw new IllegalStateException("관광공사 장소 XML 파싱에 실패했습니다.", e);
        }
    }

    public Place getPlaceDetail(Long contentId, TourApiLocale locale) {
        if (serviceKey == null || serviceKey.isBlank()) {
            throw new IllegalStateException("service-key 설정이 필요합니다.");
        }

        String url = "https://apis.data.go.kr/B551011/" + locale.serviceName() + "/detailIntro2"
                + "?serviceKey=" + encodedServiceKey()
                + "&MobileOS=WEB"
                + "&MobileApp=Gyeongjuma"
                + "&_type=xml"
                + "&contentId=" + contentId
                + "&contentTypeId=" + locale.contentTypeId();

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
            Long itemContentId = parseLong(text(item, "contentid"));

            if (itemContentId != null && !itemContentId.equals(contentId)) {
                return null;
            }

            return Place.builder()
                    .apiPlaceId(itemContentId != null ? itemContentId : contentId)
                    .tel(text(item, "infocenter"))
                    .parking(text(item, "parking"))
                    .usetime(text(item, "usetime"))
                    .restdate(text(item, "restdate"))
                    .language(locale.contentLanguage())
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("관광공사 장소 정보 XML 파싱에 실패했습니다.", e);
        }
    }

    public PlaceOverview getPlaceOverview(Long contentId, TourApiLocale locale) {
        if (serviceKey == null || serviceKey.isBlank()) {
            throw new IllegalStateException("service-key 설정이 필요합니다.");
        }

        String url = "https://apis.data.go.kr/B551011/" + locale.serviceName() + "/detailCommon2"
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
            Long itemContentId = parseLong(item.path("contentid").asText(null));

            if (itemContentId != null && !itemContentId.equals(contentId)) {
                return null;
            }

            String overview = item.path("overview").asText(null);

            if (overview == null || overview.isBlank()) {
                return null;
            }

            return new PlaceOverview(itemContentId != null ? itemContentId : contentId, overview.trim());
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

    public enum TourApiLocale {
        KOREAN("KorService2", "ko", 12),
        ENGLISH("EngService2", "en", 76),
        JAPANESE("JpnService2", "ja", 76),
        CHINESE_SIMPLIFIED("ChsService2", "zh", 76),
        CHINESE_TRADITIONAL("ChtService2", "zh-Hant", 76),
        GERMAN("GerService2", "de", 76),
        FRENCH("FreService2", "fr", 76),
        SPANISH("SpnService2", "es", 76),
        RUSSIAN("RusService2", "ru", 76);

        private final String serviceName;
        private final String contentLanguage;
        private final int contentTypeId;

        TourApiLocale(String serviceName, String contentLanguage, int contentTypeId) {
            this.serviceName = serviceName;
            this.contentLanguage = contentLanguage;
            this.contentTypeId = contentTypeId;
        }

        public String serviceName() {
            return serviceName;
        }

        public String contentLanguage() {
            return contentLanguage;
        }

        public int contentTypeId() {
            return contentTypeId;
        }
    }

    public record PlaceOverview(Long contentId, String overview) {
    }
}
