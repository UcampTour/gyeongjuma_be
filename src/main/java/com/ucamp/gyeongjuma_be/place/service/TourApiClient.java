package com.ucamp.gyeongjuma_be.place.service;

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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class TourApiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${tour.api.service-key:}")
    private String serviceKey;

    public String getPlaceXml() {
        if (serviceKey == null || serviceKey.isBlank()) {
            throw new IllegalStateException("service-key 설정이 필요합니다.");
        }

        String url = "https://apis.data.go.kr/B551011/KorService2/areaBasedList2"
                + "?serviceKey=" + serviceKey
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
