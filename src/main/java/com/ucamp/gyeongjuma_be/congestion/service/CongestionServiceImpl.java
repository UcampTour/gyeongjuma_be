package com.ucamp.gyeongjuma_be.congestion.service;

import com.ucamp.gyeongjuma_be.congestion.domain.Congestion;
import com.ucamp.gyeongjuma_be.congestion.dto.CongestionSyncResponse;
import com.ucamp.gyeongjuma_be.congestion.repository.CongestionRepository;
import com.ucamp.gyeongjuma_be.place.domain.Place;
import com.ucamp.gyeongjuma_be.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CongestionServiceImpl implements CongestionService {

    private static final String GYEONGJU_AREA_CD = "47";
    private static final String GYEONGJU_SIGNGU_CD = "47130";
    private static final int FIRST_PAGE_NO = 1;
    private static final int DEFAULT_NUM_OF_ROWS = 30;

    private final CongestionRepository congestionRepository;
    private final CongestionApiClient congestionApiClient;
    private final PlaceRepository placeRepository;

    @Override
    public List<CongestionSyncResponse> syncCongestions() {
        List<Place> places = placeRepository.findAll();

        if (places.isEmpty()) {
            return List.of();
        }

        List<Congestion> congestions = new ArrayList<>();

        for (Place place : places) {
            if (place.getPlaceName() == null || place.getPlaceName().isBlank()) {
                continue;
            }

            congestions.addAll(congestionApiClient.getCongestionList(
                    place.getPlaceId(),
                    GYEONGJU_AREA_CD,
                    GYEONGJU_SIGNGU_CD,
                    place.getPlaceName(),
                    FIRST_PAGE_NO,
                    DEFAULT_NUM_OF_ROWS
            ));
        }

        if (congestions.isEmpty()) {
            return List.of();
        }

        congestionRepository.saveAll(congestions);

        return congestions.stream()
                .map(CongestionSyncResponse::from)
                .toList();
    }
}
