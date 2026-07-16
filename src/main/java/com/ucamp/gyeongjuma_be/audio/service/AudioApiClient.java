package com.ucamp.gyeongjuma_be.audio.service;

import com.ucamp.gyeongjuma_be.audio.domain.Audio;
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
public class AudioApiClient {

    private static final int NUM_OF_ROWS = 100;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${tour.api.service-key:}")
    private String serviceKey;

    public List<Audio> searchAudios(Long placeId, String keyword) {
        if (serviceKey == null || serviceKey.isBlank()) {
            throw new IllegalStateException("tour.api.service-key 설정이 필요합니다.");
        }

        byte[] response = restTemplate.getForObject(URI.create(buildUrl(keyword)), byte[].class);
        if (response == null) {
            return List.of();
        }

        return parse(new String(response, StandardCharsets.UTF_8), placeId);
    }

    private String buildUrl(String keyword) {
        return "https://apis.data.go.kr/B551011/Odii/storySearchList"
                + "?serviceKey=" + encodedServiceKey()
                + "&numOfRows=" + NUM_OF_ROWS
                + "&pageNo=1"
                + "&MobileOS=ETC"
                + "&MobileApp=Gyeongjuma"
                + "&keyword=" + URLEncoder.encode(keyword, StandardCharsets.UTF_8)
                + "&langCode=ko";
    }

    private List<Audio> parse(String xml, Long placeId) {
        if (xml.isBlank()) {
            return List.of();
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            NodeList items = document.getElementsByTagName("item");
            List<Audio> audios = new ArrayList<>();

            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                String audioUrl = text(item, "audioUrl");
                if (audioUrl == null) {
                    continue;
                }

                audios.add(Audio.builder()
                        .placeId(placeId)
                        .audioUrl(audioUrl)
                        .imageUrl(text(item, "imageUrl"))
                        .title(text(item, "title"))
                        .script(text(item, "script"))
                        .playTime(text(item, "playTime"))
                        .build());
            }
            return audios;
        } catch (Exception e) {
            throw new IllegalStateException("Odii 오디오 데이터 파싱에 실패했습니다.", e);
        }
    }

    private String encodedServiceKey() {
        String key = serviceKey.trim();
        return key.contains("%") ? key : URLEncoder.encode(key, StandardCharsets.UTF_8);
    }

    private String text(Element element, String tagName) {
        NodeList nodes = element.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return null;
        }

        String value = nodes.item(0).getTextContent();
        return value == null || value.isBlank() ? null : value.trim();
    }
}
